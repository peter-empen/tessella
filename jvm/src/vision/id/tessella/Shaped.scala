package vision.id.tessella

import scala.collection.Set
import scala.language.{higherKinds, postfixOps}
import scala.util.{Failure, Success, Try}

import scalax.collection.GraphEdge.UnDiEdge
import scalax.collection.GraphPredef._
import scalax.collection.constrained.PreCheckFollowUp._
import scalax.collection.constrained._

import vision.id.tessella.Polar.{PointPolar, UnitSimplePgon}
import vision.id.tessella.Tau.TAU

/** Ensures that the underlying `Graph` is a valid tessellation.
  */
class Shaped[N, E[X] <: EdgeLikeIn[X]](override val self: Graph[N, E])
    extends Constraint[N, E](self)
    with NodeChecks[N, E]
    with Perimeter
    with Neighbors {

  /** Skips this pre-check to rely on the post-check `postAdd` except for trivial cases. */
  override def preCreate(nodes: Traversable[N], edges: Traversable[E[N]]) =
    PreCheckResult(PostCheck)

  /** Adding one node can never result into a valid tessellation. */
  override def preAdd(node: N): PreCheckResult =
    PreCheckResult(Abort)

  /** Only in very rare cases (to be analyzed) adding one edge can result into a valid tessellation. */
  override def preAdd(edge: E[N]): PreCheckResult =
    PreCheckResult(Abort)

  /** To be analyzed, for the time being skip */
  override def preAdd(elems: InParam[N, E]*): PreCheckResult =
    PreCheckResult(PostCheck)

  private implicit final class XShaped(graph: Graph[Int, UnDiEdge]) {

    private implicit class XNode(node: graph.NodeT) {

      def neighs: List[(Int, List[Int])] = graph.outerNodeHood(node.toOuter, periNodes)
    }

    def periNodes: List[Int] = graph.perimeterNodesEdges match { case (periNodes, _) => periNodes }

    def perimeterSimplePolygon: Try[UnitSimplePgon] = Try {

      val periNodes = graph.periNodes

      val (_, periPaths): (List[List[Int]], List[List[List[Int]]]) =
        periNodes.init.map(graph.outerNodeHood(_, periNodes).unzip).unzip

      val vertexes: List[Vertex] = periPaths.map(paths => Vertex.p(paths.init.map(_.size + 2)))

      // implies satisfying requirements of UnitSimplePgon
      new UnitSimplePgon(vertexes.map(v => new PointPolar(1.0, TAU / 2 - v.alpha)))

    }

    def toTessellMap: Try[NodesMap] = {

      val size = graph.nodes.size

      def loop(tm: NodesMap): Try[NodesMap] = {

        if (tm.m.size == size) Success(tm)
        else {
          val mapped: List[Int] = tm.m.keys.toList
          val nexttm: Try[NodesMap] = graph.findCompletable(mapped, tm) match {
            case Some(node) => tm.completeNode(node, (graph get node).neighs)
            case None =>
              graph.findAddable(mapped) match {
                case Some(node) => tm.addFromNeighbors(node, (graph get node).neighs)
                case None       => perimeterSimplePolygon.flatMap(polygon => tm.addFromPerimeter(periNodes.init, polygon.pps))
              }
          }
          loop(nexttm.get)
        }
      }

      val firstNode: graph.NodeT = graph.nodes.minBy(_.toOuter)
      loop(NodesMap.firstThree(firstNode.toOuter, firstNode.neighs))
    }

    def hasGap: Boolean = graph.toTessellMap match {
      case Success(tm) => graph.edges.exists(edge => !tm.m(edge._n(0).toOuter).isUnitDistance(tm.m(edge._n(1).toOuter)))
      case Failure(_)  => true
    }

  }

  private def refusal(msg: String): Nothing =
    throw new IllegalArgumentException("Addition refused: " + msg)

  /** Check the whole `newGraph`. */
  override def postAdd(newGraph: Graph[N, E],
                       passedNodes: Traversable[N],
                       passedEdges: Traversable[E[N]],
                       preCheck: PreCheckResult): Boolean = {
    if (!newGraph.hasPositiveValues)
      refusal("non positive nodes = " + newGraph.nodes.filter(_.toOuter match {
        case i: Int => i <= 0
        case _      => false
      }))
    if (!newGraph.hasRegularNodes)
      refusal(
        "nodes with wrong number of edges = " +
          newGraph.nodes.filter(node => node.degree < 2 || node.degree > 6))
    if (!newGraph.isConnected)
      refusal("graph not connected")
    val g = newGraph.asInstanceOf[Graph[Int, UnDiEdge]]
    if (g.perimeterSimplePolygon.isFailure)
      refusal("perimeter is not a simple polygon")
    if (g.hasGap)
      refusal("tiling with gap")
    true
  }

  /** Only in very rare cases (to be analyzed) subtracting one node (and related edges)
    * can result into a valid tessellation. */
  override def preSubtract(node: self.NodeT, forced: Boolean): PreCheckResult =
    PreCheckResult(Abort)

  /** Only in very rare cases (to be analyzed) subtracting one edge can result into a valid tessellation. */
  override def preSubtract(edge: self.EdgeT, simple: Boolean): PreCheckResult =
    PreCheckResult(Abort)

  /** To be analyzed, for the time being skip */
  override def preSubtract(nodes: => Set[self.NodeT], edges: => Set[self.EdgeT], simple: Boolean): PreCheckResult =
    PreCheckResult(PostCheck)

  override def onAdditionRefused(refusedNodes: Traversable[N],
                                 refusedEdges: Traversable[E[N]],
                                 graph: Graph[N, E]): Boolean =
    refusal("nodes = " + refusedNodes + ", " + "edges = " + refusedEdges)

}

object Shaped extends ConstraintCompanion[Shaped] {
  def apply[N, E[X] <: EdgeLikeIn[X]](self: Graph[N, E]): Shaped[N, E] = new Shaped[N, E](self)
}
