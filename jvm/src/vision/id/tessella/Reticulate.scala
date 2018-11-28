package vision.id.tessella

import scalax.collection.Graph
import scalax.collection.GraphEdge.UnDiEdge
import scalax.collection.GraphPredef._

import vision.id.tessella.TessellGraph.Tessell

/**
  * fast methods to create reticulate tessellations of arbitrary size
  */
trait Reticulate extends GraphUtils {

  /**
    * compose a rectangular reticulate of x by y squares
    *
    * @param x side units
    * @param y side units
    * @return
    */
  def squareNet(x: Int, y: Int): Tessell = {
    require(x > 0 && y > 0)

    val horiz = for {
      i ← 1 to x
      j ← 0 to y
      h = i + (x + 1) * j
    } yield (h, h + 1)
    val vert = for {
      i ← 1 to (x + 1)
      j ← 0 until y
    } yield (i + (x + 1) * j, i + (x + 1) * (j + 1))
    new Tessell((horiz ++ vert).foldLeft(Graph(): Graph[Int, UnDiEdge])((es, p) ⇒ es + p._1 ~ p._2))
  }

  /**
    * compose a rectangular reticulate of x by y triangles
    *
    * @param x side units
    * @param y side units
    * @return
    */
  def triangleNet(x: Int, y: Int): Tessell = {
    require(x > 0 && y > 0)
    require(x % 2 == 0)

    val h = x / 2
    val diag = for {
      i ← 1 to h
      j ← 0 until y
    } yield (i + (h + 1) * j, i + 1 + (h + 1) * (j + 1))
    new Tessell(diag.foldLeft(squareNet(h, y).graph)((es, p) ⇒ es + p._1 ~ p._2))
  }

  /**
    * compose a rectangular reticulate of x by y hexagons
    *
    * @param x side units
    * @param y side units
    * @return
    */
  def hexagonNet(x: Int, y: Int): Tessell = {
    require(x > 0 && y > 0)

    val horiz = for {
      i ← 0 to x * 2
      j ← 0 to y
      h = i + (x + 1) * j * 2
    } yield (h, h + 1)
    val vert = for {
      i ← 1 to (x + 1)
      j ← 0 until y
      v = (x + 1) * 2; w = i * 2 - 1
    } yield (w + v * j, w - 1 + v * (j + 1))
    new Tessell(
      (horiz.tail.init ++ vert)
        .foldLeft(Graph(): Graph[Int, UnDiEdge])((es, p) ⇒ es + p._1 ~ p._2))
  }

  /**
    * build variants of triangle grid by emptying hex according to function
    *
    * @param x number of triangles on the x-axis
    * @param y number of triangles on the y-axis
    * @param f function telling if given ij node must be deleted or not
    * @return
    */
  private def triangleNetVariant(x: Int, y: Int, f: (Int, Int) ⇒ Boolean): Tessell = {
    val h = x / 2
    val emptyNodes = for {
      i ← 1 to h + 1
      j ← 0 to y
      if f(i, j)
    } yield i + (h + 1) * j
    val newg    = emptyNodes.foldLeft(triangleNet(x, y).graph)(_ - _)
    val orphans = newg.nodes filter (_.degree == 1)

    def cutCorners(g: Graph[Int, UnDiEdge], bridges: List[Int], corners: List[Int]): Graph[Int, UnDiEdge] = {
      if (g.nodes.toList.map(_.toOuter).intersect(bridges).isEmpty)
        g -- Graph.from(corners, Nil).nodes
      else
        g
    }

    val bottomleft = List(1 + (y - 2) * (h + 1), 3 + y * (h + 1))
    val bl_corners = List(1 + (y - 1) * (h + 1), 2 + y * (h + 1), 1 + y * (h + 1))

    val topright   = List(3 * (h + 1), h - 1)
    val tr_corners = List(2 * (h + 1), h, h + 1)

    new Tessell(cutCorners(cutCorners(newg -- orphans, bottomleft, bl_corners), topright, tr_corners).renumber())
  }

  /**
    * uniform tessellation (▲.⬣.▲.⬣) (t=2, e=1)
    */
  def uniform(x: Int, y: Int): Tessell =
    triangleNetVariant(x, y, _ % 2 == 1 && _ % 2 == 1)

  /**
    * uniform tessellation (▲⁴.⬣) (t=3, e=3)
    */
  def uniform2(x: Int, y: Int): Tessell =
    triangleNetVariant(x, y, (i, j) ⇒ (i + 2 * j) % 7 == 0)

  /**
    * 2-uniform tessellation (▲⁶; ▲⁴.⬣) (t=5, e=7)
    */
  def twoUniform3(x: Int, y: Int): Tessell =
    triangleNetVariant(x, y, (i, j) ⇒ (i + 3 * j) % 13 == 0)

  /**
    * 2-uniform tessellation (▲⁴.⬣; ▲².⬣²) (t=2, e=4)
    */
  def twoUniform4(x: Int, y: Int): Tessell =
    triangleNetVariant(x, y, (i, j) ⇒ (i + 3 * j) % 5 == 0)

  /**
    * 2-uniform tessellation (▲.⬣.▲.⬣; ▲².⬣²) (t=2, e=3)
    */
  def twoUniform5(x: Int, y: Int): Tessell =
    triangleNetVariant(x, y, (i, j) ⇒ (i + 2 * j) % 4 == 0)

  /**
    * 3-uniform tessellation (▲².⬣²; ▲.⬣.▲.⬣; ⬣³) (t=4, e=5)
    */
  def threeUniformOneOneOne4(x: Int, y: Int): Tessell = {
    val f: (Int, Int) ⇒ Boolean = (i, j) ⇒
      j % 5 match {
        case e if e == 2 || e == 4 ⇒ i           % 5 == (e / 2 + 1) || i % 5 == (e / 2 - 1)
        case _                     ⇒ (i + 2 * j) % 5 == 0
    }
    triangleNetVariant(x, y, f)
  }

  /**
    * 3-uniform tessellation (▲².⬣²; ▲.⬣.▲.⬣; ⬣³) (t=2, e=4)
    */
  def threeUniformOneOneOne5(x: Int, y: Int): Tessell =
    triangleNetVariant(x, y, (i, j) ⇒ (i + 3 * j) % 7 == 0 || (i + 3 * j) % 7 == 2)

  /**
    * 3-uniform tessellation (▲⁶; ▲⁴.⬣; ▲.⬣.▲.⬣) (t=5, e=6)
    */
  def threeUniformOneOneOne6(x: Int, y: Int): Tessell =
    triangleNetVariant(x, y, (i, j) ⇒ (i + 4 * j) % 8 == 0)

  /**
    * 4-uniform tessellation ([2x ▲⁶]; ▲⁴.⬣; ▲².⬣²)
    */
  def fourUniformTwoOneOne8(x: Int, y: Int): Tessell =
    triangleNetVariant(x, y, (i, j) ⇒ (i + 1 * j) % 8 == 0)

  /**
    * get nodes of a single hex on the hexagonNet
    *
    * @param x number of hexs on the x-axis
    * @param i cell coord on the x-axis (0 first)
    * @param j cell coord on the y-axis (0 first)
    * @return
    */
  private def hexagonNetCellNodes(x: Int, i: Int, j: Int): List[Int] =
    for {
      q ← List(0, 1)
      p ← List(0, 1, 2)
    } yield (j + q) * ((x + 1) * 2) + i * 2 + p + 1 - q

  /**
    * build variants of hex grid by filling hex with ▲⁶ according to function
    *
    * @param x number of hexs on the x-axis
    * @param y number of hexs on the y-axis
    * @param f function telling if given ij hex must be filled or not
    * @return
    */
  private def hexagonNetVariant(x: Int, y: Int, f: (Int, Int) ⇒ Boolean): Tessell = {
    val totNodes = (y + 1) * 2 * (x + 1) - 2
    val hexNodes = for {
      i ← 0 until x
      j ← 0 until y
      if f(i, j)
    } yield hexagonNetCellNodes(x, i, j)
    val (newg, _) = hexNodes.foldLeft((hexagonNet(x, y).graph, totNodes + 1))({
      case ((g, centre), hex) ⇒
        (hex.foldLeft(g)((h, hexNode) ⇒ h + centre ~ hexNode), centre + 1)
    })
    new Tessell(newg)
  }

  /**
    * 2-uniform tessellation (▲⁶; ▲².⬣²) (t=2, e=3)
    *
    * @see https://en.wikipedia.org/wiki/Euclidean_tilings_by_convex_regular_polygons#2-uniform_tilings
    */
  def twoUniform(x: Int, y: Int): Tessell =
    hexagonNetVariant(x, y, _ % 3 == _ % 3)

  /**
    * 2-uniform tessellation (▲⁶; ▲⁴.⬣) (t=3, e=3)
    *
    * @see https://en.wikipedia.org/wiki/Euclidean_tilings_by_convex_regular_polygons#2-uniform_tilings
    */
  def twoUniform2(x: Int, y: Int): Tessell =
    hexagonNetVariant(x, y, _ % 3 != _ % 3)
  //triangleNetVariant(x, y, _ % 3 == 0 && _ % 3 == 0)

  /**
    * 3-uniform tessellation (▲⁶; ⬣³; ▲².⬣²) (t=2, e=3)
    *
    * @see https://en.wikipedia.org/wiki/Euclidean_tilings_by_convex_regular_polygons#3-uniform_tilings,_3_vertex_types
    */
  def threeUniformOneOneOne(x: Int, y: Int): Tessell =
    hexagonNetVariant(x, y, _ % 2 == 0 && _ % 2 == 0)

  /**
    * 3-uniform tessellation (▲⁶; ▲⁴.⬣; ▲².⬣²) (t=5, e=8)
    *
    * @see https://en.wikipedia.org/wiki/Euclidean_tilings_by_convex_regular_polygons#3-uniform_tilings,_3_vertex_types
    */
  def threeUniformOneOneOne2(x: Int, y: Int): Tessell =
    hexagonNetVariant(x, y, (i, j) ⇒ (i + 2 * j) % 4 < 2)

  /**
    * 3-uniform tessellation (▲⁶; ▲⁴.⬣; ▲².⬣²) (t=3, e=5)
    *
    * @see https://en.wikipedia.org/wiki/Euclidean_tilings_by_convex_regular_polygons#3-uniform_tilings,_3_vertex_types
    */
  def threeUniformOneOneOne3(x: Int, y: Int): Tessell =
    hexagonNetVariant(x, y, (_, j) ⇒ j % 2 == 0)

  /**
    * 3-uniform tessellation ([2x ▲⁶]; ▲⁴.⬣) (t=3, e=4)
    *
    * @see https://en.wikipedia.org/wiki/Euclidean_tilings_by_convex_regular_polygons#3-uniform_tilings,_2_vertex_types_(2:1)
    */
  def threeUniformTwoOne(x: Int, y: Int): Tessell =
    hexagonNetVariant(x, y, _ % 2 == 0 || _ % 2 == 0)

  /**
    * 4-uniform tessellation (▲⁶; ▲⁴.⬣; ▲².⬣²; ⬣³)
    *
    * @see https://en.wikipedia.org/wiki/Euclidean_tilings_by_convex_regular_polygons#4-uniform_tilings,_4_vertex_types
    */
  def fourUniformOneOneOneOne(x: Int, y: Int): Tessell =
    hexagonNetVariant(x, y, (i, j) ⇒ (i + 2 * j) % 6 < 2)

  /**
    * 4-uniform tessellation (▲⁶; ▲⁴.⬣; ▲².⬣²; ⬣³)
    *
    * @see https://en.wikipedia.org/wiki/Euclidean_tilings_by_convex_regular_polygons#4-uniform_tilings,_4_vertex_types
    */
  def fourUniformOneOneOneOne1(x: Int, y: Int): Tessell =
    hexagonNetVariant(x, y, (i, j) ⇒ (i + 2 * j) % 5 < 2)

  /**
    * 4-uniform tessellation (▲⁶; ▲⁴.⬣; ▲².⬣²; ⬣³)
    *
    * @see https://en.wikipedia.org/wiki/Euclidean_tilings_by_convex_regular_polygons#4-uniform_tilings,_4_vertex_types
    */
  def fourUniformOneOneOneOne2(x: Int, y: Int): Tessell =
    hexagonNetVariant(x, y, (_, j) ⇒ j % 3 == 0)

  /**
    * 4-uniform tessellation (▲⁶; ▲².⬣²; [2x ⬣³])
    *
    * @see https://en.wikipedia.org/wiki/Euclidean_tilings_by_convex_regular_polygons#4-uniform_tilings,_3_vertex_types_(2:1:1)
    */
  def fourUniformTwoOneOne(x: Int, y: Int): Tessell =
    hexagonNetVariant(x, y, (i, j) ⇒ i % 7 == (j * 4) % 7)

  /**
    * 4-uniform tessellation (▲⁶; ▲².⬣²; [2x ⬣³])
    *
    * @see https://en.wikipedia.org/wiki/Euclidean_tilings_by_convex_regular_polygons#4-uniform_tilings,_3_vertex_types_(2:1:1)
    * @see http://probabilitysports.com/tilings.html?u=0&n=4&t=1
    */
  def fourUniformTwoOneOne2(x: Int, y: Int): Tessell =
    hexagonNetVariant(x, y, (i, j) ⇒ j % 2 == 0 && i % 6 == (j * 4) % 6)

  /**
    * 4-uniform tessellation (▲⁶; ▲².⬣²; [2x ⬣³])
    *
    * @see https://en.wikipedia.org/wiki/Euclidean_tilings_by_convex_regular_polygons#4-uniform_tilings,_3_vertex_types_(2:1:1)
    */
  def fourUniformTwoOneOne3(x: Int, y: Int): Tessell =
    hexagonNetVariant(x, y, (i, j) ⇒ ((i + 2 * j) % 6 == 0 || (i + 2 * j) % 6 == 2) && j % 2 == 0)

  /**
    * 4-uniform tessellation (▲⁶; [2x ▲².⬣²]; ⬣³)
    *
    * @see https://en.wikipedia.org/wiki/Euclidean_tilings_by_convex_regular_polygons#4-uniform_tilings,_3_vertex_types_(2:1:1)
    */
  def fourUniformTwoOneOne4(x: Int, y: Int): Tessell =
    hexagonNetVariant(x, y, (i, j) ⇒ (i + 2 * j) % 3 == 0 && j % 3 < 2)

  /**
    * 4-uniform tessellation (▲⁶; [2x ▲².⬣²]; ⬣³)
    *
    * @see https://en.wikipedia.org/wiki/Euclidean_tilings_by_convex_regular_polygons#4-uniform_tilings,_3_vertex_types_(2:1:1)
    */
  def fourUniformTwoOneOne5(x: Int, y: Int): Tessell =
    hexagonNetVariant(x, y, (i, j) ⇒ {
      // alternate rows between 3 and 6
      val step = (j % 2 + 1) * 3
      (i + 2 * j) % step == 0
    })

  /**
    * 4-uniform tessellation (▲⁶; ▲².⬣²; [2x ⬣³])
    *
    * @see https://en.wikipedia.org/wiki/Euclidean_tilings_by_convex_regular_polygons#4-uniform_tilings,_3_vertex_types_(2:1:1)
    */
  def fourUniformTwoOneOne6(x: Int, y: Int): Tessell =
    hexagonNetVariant(x, y, _ % 3 == 0 && _ % 3 == 0)

  /**
    * 4-uniform tessellation (▲⁶; [2x ▲⁴.⬣]; ▲².⬣²)
    *
    * @see https://en.wikipedia.org/wiki/Euclidean_tilings_by_convex_regular_polygons#4-uniform_tilings,_3_vertex_types_(2:1:1)
    */
  def fourUniformTwoOneOne7(x: Int, y: Int): Tessell =
    hexagonNetVariant(x, y, (i, j) ⇒ {
      val pos = i + 2 * j
      if (j % 2 == 0)
        pos % 3 < 2
      else
        pos % 6 == 0 || pos % 6 == 4
    })

  /**
    * 5-uniform tessellation ([2x ▲⁶]; ▲⁴.⬣; ▲².⬣²; ⬣³)
    *
    * @see https://en.wikipedia.org/wiki/Euclidean_tilings_by_convex_regular_polygons#5-uniform_tilings,_4_vertex_types_(2:1:1:1)
    */
  def fiveUniformTwoOneOneOne(x: Int, y: Int): Tessell =
    hexagonNetVariant(x, y, (i, j) ⇒ {
      val pos = i + 2 * j
      if (j % 2 == 0)
        pos % 3 == 1
      else
        pos % 6 < 3
    })

  /**
    * 5-uniform tessellation (▲⁶; ▲⁴.⬣; ▲².⬣²; [2x ⬣³])
    *
    * @see https://en.wikipedia.org/wiki/Euclidean_tilings_by_convex_regular_polygons#5-uniform_tilings,_4_vertex_types_(2:1:1:1)
    */
  def fiveUniformTwoOneOneOne2(x: Int, y: Int): Tessell =
    hexagonNetVariant(x, y, (i, j) ⇒ (i + 2 * j) % 8 < 2)

  /**
    * 5-uniform tessellation (▲⁶; ▲⁴.⬣; [2x ▲².⬣²]; ⬣³)
    *
    * @see https://en.wikipedia.org/wiki/Euclidean_tilings_by_convex_regular_polygons#5-uniform_tilings,_4_vertex_types_(2:1:1:1)
    */
  def fiveUniformTwoOneOneOne3(x: Int, y: Int): Tessell =
    hexagonNetVariant(x, y, (i, j) ⇒ {
      val pos = i + 2 * j
      pos % 7 == 0 || pos % 7 == 2
    })

  /**
    * 5-uniform tessellation (▲⁶; ▲⁴.⬣; ▲².⬣²; [2x ⬣³])
    *
    * @see https://en.wikipedia.org/wiki/Euclidean_tilings_by_convex_regular_polygons#5-uniform_tilings,_4_vertex_types_(2:1:1:1)
    */
  def fiveUniformTwoOneOneOne4(x: Int, y: Int): Tessell =
    hexagonNetVariant(x, y, (i, j) ⇒ (i + 2 * j) % 7 < 2)

  /**
    * 5-uniform tessellation (▲⁶; ▲⁴.⬣; ▲².⬣²; [2x ⬣³])
    *
    * @see https://en.wikipedia.org/wiki/Euclidean_tilings_by_convex_regular_polygons#5-uniform_tilings,_4_vertex_types_(2:1:1:1)
    */
  def fiveUniformTwoOneOneOne5(x: Int, y: Int): Tessell =
    hexagonNetVariant(x, y, (i, j) ⇒ (i + j) % 4 == 0)

  /**
    * 5-uniform tessellation (▲⁶; ▲⁴.⬣; [2x ▲².⬣²]; ⬣³)
    *
    * @see https://en.wikipedia.org/wiki/Euclidean_tilings_by_convex_regular_polygons#5-uniform_tilings,_4_vertex_types_(2:1:1:1)
    */
  def fiveUniformTwoOneOneOne6(x: Int, y: Int): Tessell =
    hexagonNetVariant(x, y, (i, j) ⇒ {
      val pos = i + 2 * j
      pos % 6 == 0 || pos % 6 == 2
    })

  /**
    * 5-uniform tessellation (▲⁶; [3x ▲².⬣²]; ⬣³)
    *
    * @see https://en.wikipedia.org/wiki/Euclidean_tilings_by_convex_regular_polygons#5-uniform_tilings,_3_vertex_types_(3:1:1)_and_(2:2:1)
    */
  def fiveUniformThreeOneOne(x: Int, y: Int): Tessell =
    hexagonNetVariant(x, y, (i, j) ⇒ {
      val pos = i + 2 * j
      pos % 8 == 0 || pos % 8 == 3
    })

  /**
    * 5-uniform tessellation (▲⁶; [3x ▲².⬣²]; ⬣³)
    *
    * @see https://en.wikipedia.org/wiki/Euclidean_tilings_by_convex_regular_polygons#5-uniform_tilings,_3_vertex_types_(3:1:1)_and_(2:2:1)
    */
  def fiveUniformThreeOneOne2(x: Int, y: Int): Tessell =
    hexagonNetVariant(x, y, (i, j) ⇒ {
      val pos = i + 2 * j
      pos % 7 == 0 || pos % 7 == 3
    })

  /**
    * 5-uniform tessellation ([3x ▲⁶]; ▲⁴.⬣; ▲².⬣²)
    *
    * @see https://en.wikipedia.org/wiki/Euclidean_tilings_by_convex_regular_polygons#5-uniform_tilings,_3_vertex_types_(3:1:1)_and_(2:2:1)
    */
  def fiveUniformThreeOneOne3(x: Int, y: Int): Tessell =
    hexagonNetVariant(x, y, (i, j) ⇒ (i + 2 * j) % 6 > 1)

  /**
    * 5-uniform tessellation ([3x ▲⁶]; ▲⁴.⬣; ▲².⬣²)
    *
    * @see https://en.wikipedia.org/wiki/Euclidean_tilings_by_convex_regular_polygons#5-uniform_tilings,_3_vertex_types_(3:1:1)_and_(2:2:1)
    */
  def fiveUniformThreeOneOne4(x: Int, y: Int): Tessell =
    hexagonNetVariant(x, y, (i, j) ⇒ {
      val pos = i + 2 * j
      if (j % 2 == 0)
        pos % 3 < 2
      else
        pos % 6 < 1 || pos % 6 > 3
    })

  /**
    * 5-uniform tessellation (▲⁶; [2x ▲².⬣²]; [2x ⬣³])
    *
    * @see https://en.wikipedia.org/wiki/Euclidean_tilings_by_convex_regular_polygons#5-uniform_tilings,_3_vertex_types_(3:1:1)_and_(2:2:1)
    * @see http://probabilitysports.com/tilings.html?u=0&n=5&t=2
    */
  def fiveUniformTwoTwoOne(x: Int, y: Int): Tessell =
    hexagonNetVariant(x, y, (i, j) ⇒ i % 7 == (j * 3) % 7)

  /**
    * 5-uniform tessellation ([2x ▲⁶]; ▲⁴.⬣; [2x ▲².⬣²])
    *
    * @see https://en.wikipedia.org/wiki/Euclidean_tilings_by_convex_regular_polygons#5-uniform_tilings,_3_vertex_types_(3:1:1)_and_(2:2:1)
    */
  def fiveUniformTwoTwoOne2(x: Int, y: Int): Tessell =
    hexagonNetVariant(x, y, (i, j) ⇒ i % 3 == j % 3 || (i % 3 == 0 && j % 3 == 1))

  /**
    * 5-uniform tessellation (▲⁶; [2x ▲².⬣²]; [2x ⬣³])
    *
    * @see https://en.wikipedia.org/wiki/Euclidean_tilings_by_convex_regular_polygons#5-uniform_tilings,_3_vertex_types_(3:1:1)_and_(2:2:1)
    */
  def fiveUniformTwoTwoOne3(x: Int, y: Int): Tessell =
    hexagonNetVariant(x, y, (i, j) ⇒ (i + 2 * j) % 6 == 0)

  /**
    * 5-uniform tessellation ([2x ▲⁶]; [2x ▲⁴.⬣]; ▲².⬣²)
    *
    * @see https://en.wikipedia.org/wiki/Euclidean_tilings_by_convex_regular_polygons#5-uniform_tilings,_3_vertex_types_(3:1:1)_and_(2:2:1)
    */
  def fiveUniformTwoTwoOne4(x: Int, y: Int): Tessell =
    hexagonNetVariant(x, y, (i, j) ⇒ {
      val pos = i + 2 * j
      pos % 5 == 1 || pos % 5 > 2
    })

  /**
    * 5-uniform tessellation ([4x ▲⁶]; ▲⁴.⬣)
    *
    * @see https://en.wikipedia.org/wiki/Euclidean_tilings_by_convex_regular_polygons#5-uniform_tilings,_2_vertex_types_(4:1)_and_(3:2)
    */
  def fiveUniformFourOne(x: Int, y: Int): Tessell =
    hexagonNetVariant(x, y, _ % 3 != 0 || _ % 3 != 0)

  /**
    * 6-uniform tessellation (▲⁶; ▲².⬣²; [4x ⬣³])
    *
    * @see http://probabilitysports.com/tilings.html?u=0&n=6&t=2
    */
  def sixUniformFourOneOne(x: Int, y: Int): Tessell =
    hexagonNetVariant(x, y, (i, j) ⇒ i % 10 == (j * 8) % 10)

}
