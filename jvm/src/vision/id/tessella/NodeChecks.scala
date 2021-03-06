package vision.id.tessella

import scalax.collection.GraphPredef.EdgeLikeIn
import scalax.collection.constrained.{CompanionAlias, Graph}

import scala.language.higherKinds

trait NodeChecks[N, E[X] <: EdgeLikeIn[X]] {

  final implicit class XGraph(graph: Graph[N, E]) {

    def hasPositiveValues: Boolean =
      graph.nodes.forall(_.toOuter match {
        case i: Int => i > 0
        case _      => false
      })

    def hasRegularNodes: Boolean = graph.nodes.forall(_.isRegular)

    final implicit class XNode(node: graph.NodeT) {

      def isRegular: Boolean = {
        val d: Int = node.degree
        d >= 2 && d <= 6
      }

    }
  }

}
