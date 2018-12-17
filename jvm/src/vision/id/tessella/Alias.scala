package vision.id.tessella

import scalax.collection.GraphEdge._
import scalax.collection.constrained._
import scalax.collection.GraphPredef._
import scalax.collection.constrained.CompanionAlias

object Alias {

  type Tiling = Graph[Int, UnDiEdge]

  implicit val conf: Config = Shaped

  object Tiling extends CompanionAlias[UnDiEdge](Shaped withStringPrefix "Tiling") with Net {

    def poly(sides: Int): Tiling = Tiling.from(Nil, for (i ← 1 to sides) yield i ~ (i % sides + 1))

    def fromG(graph: scalax.collection.Graph[Int, UnDiEdge]): Tiling = Tiling.from(graph.nodes, graph.edges)

  }

}