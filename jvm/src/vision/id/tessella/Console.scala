package vision.id.tessella

import com.typesafe.scalalogging.Logger

import scalax.collection.Graph
import scalax.collection.GraphPredef._

import vision.id.tessella.Alias.Tiling

object Console extends SVG {

  val logger = Logger("CONSOLE")

  /**
    * Launches main app
    *
    * @param args possible arguments
    * @return nothing
    */
  def main(args: Array[String]): Unit = {

    args.toList match {
      case "output" :: p2 ⇒
        p2 match {
          case action :: Nil ⇒ output(action)
          case _             ⇒ logger.error("Command `output` needs 1 param: action")
        }
      case Nil ⇒ logger.error("Please add command")
      case _   ⇒ logger.error("Unknown command")
    }

  }

  def output(action: String): Unit = {

    def algos(): Unit = {
      val m = Map(
        "regular/(▲⁶)"                               → Tiling.triangleNet(24, 16),
        "regular/(■⁴)"                               → Tiling.squareNet(8, 8),
        "regular/(⬣³)"                               → Tiling.hexagonNet(8, 8),
        "uniform/(▲⁴.⬣)"                             → Tiling.uniform2(24, 16),
        "uniform/(▲³.■²)"                            → Tiling.elongatedTriangular(12, 12),
        "uniform/(▲.⬣.▲.⬣)"                          → Tiling.uniform(24, 16),
        "2-uniform/(▲⁶; ▲⁴.⬣)"                       → Tiling.twoUniform2(8, 8),
        "2-uniform/(▲⁶; ▲⁴.⬣)_alt"                   → Tiling.twoUniform3(24, 16),
        "2-uniform/(▲⁶; ▲².⬣²)"                      → Tiling.twoUniform(8, 8),
        "2-uniform/(▲⁴.⬣; ▲².⬣²)"                    → Tiling.twoUniform4(24, 16),
        "2-uniform/(▲.⬣.▲.⬣; ▲².⬣²)"                 → Tiling.twoUniform5(24, 16),
        "3-uniform/(▲⁶; ⬣³; ▲².⬣²)"                  → Tiling.threeUniformOneOneOne(8, 8),
        "3-uniform/(▲⁶; ▲⁴.⬣; ▲².⬣²)"                → Tiling.threeUniformOneOneOne2(8, 8),
        "3-uniform/(▲⁶; ▲⁴.⬣; ▲².⬣²)_alt"            → Tiling.threeUniformOneOneOne3(8, 8),
        "3-uniform/(▲⁶; ▲⁴.⬣; ▲.⬣.▲.⬣)"              → Tiling.threeUniformOneOneOne6(24, 16),
        "3-uniform/(▲⁴.⬣; ▲³.■²; ▲.■².⬣)"            → Tiling.threeUniformOneOneOne7(12, 12),
        "3-uniform/(▲³.■²; ▲².⬣²; ▲.■².⬣)"           → Tiling.threeUniformOneOneOne8(12, 12),
        "3-uniform/(▲².⬣²; ▲.⬣.▲.⬣; ⬣³)"             → Tiling.threeUniformOneOneOne4(24, 16),
        "3-uniform/(▲².⬣²; ▲.⬣.▲.⬣; ⬣³)_alt"         → Tiling.threeUniformOneOneOne5(24, 16),
        "3-uniform/([2x ▲⁶]; ▲⁴.⬣)"                  → Tiling.threeUniformTwoOne(8, 8),
        "4-uniform/(▲⁶; ▲⁴.⬣; ▲².⬣²; ⬣³)"            → Tiling.fourUniformOneOneOneOne(8, 8),
        "4-uniform/(▲⁶; ▲⁴.⬣; ▲².⬣²; ⬣³)_alt"        → Tiling.fourUniformOneOneOneOne1(8, 8),
        "4-uniform/(▲⁶; ▲⁴.⬣; ▲².⬣²; ⬣³)_alt2"       → Tiling.fourUniformOneOneOneOne2(8, 8),
        "4-uniform/(▲⁶; [2x ⬣³]; ▲².⬣²)"             → Tiling.fourUniformTwoOneOne(8, 8),
        "4-uniform/(▲⁶; [2x ⬣³]; ▲².⬣²)_alt"         → Tiling.fourUniformTwoOneOne2(8, 8),
        "4-uniform/(▲⁶; ▲².⬣²; [2x ⬣³])_alt2"        → Tiling.fourUniformTwoOneOne3(8, 8),
        "4-uniform/(▲⁶; [2x ▲².⬣²]; ⬣³)"             → Tiling.fourUniformTwoOneOne4(8, 8),
        "4-uniform/(▲⁶; [2x ▲².⬣²]; ⬣³)_alt"         → Tiling.fourUniformTwoOneOne5(8, 8),
        "4-uniform/(▲⁶; ▲².⬣²; [2x ⬣³])_alt3"        → Tiling.fourUniformTwoOneOne6(8, 8),
        "4-uniform/(▲⁶; [2x ▲⁴.⬣]; ▲².⬣²)"           → Tiling.fourUniformTwoOneOne7(8, 8),
        "4-uniform/([2x ▲⁶]; ▲⁴.⬣; ▲².⬣²)"           → Tiling.fourUniformTwoOneOne8(24, 16),
        "5-uniform/([2x ▲⁶]; ▲⁴.⬣; ▲².⬣²; ⬣³)"       → Tiling.fiveUniformTwoOneOneOne(8, 8),
        "5-uniform/(▲⁶; ▲⁴.⬣; ▲².⬣²; [2x ⬣³])"       → Tiling.fiveUniformTwoOneOneOne2(8, 8),
        "5-uniform/(▲⁶; ▲⁴.⬣; [2x ▲².⬣²]; ⬣³)"       → Tiling.fiveUniformTwoOneOneOne3(8, 8),
        "5-uniform/(▲⁶; ▲⁴.⬣; ▲².⬣²; [2x ⬣³])_alt"   → Tiling.fiveUniformTwoOneOneOne4(8, 8),
        "5-uniform/(▲⁶; ▲⁴.⬣; ▲².⬣²; [2x ⬣³])_alt2"  → Tiling.fiveUniformTwoOneOneOne5(8, 8),
        "5-uniform/(▲⁶; ▲⁴.⬣; ▲².⬣²; [2x ⬣³])_alt3"  → Tiling.fiveUniformTwoOneOneOne6(8, 8),
        "5-uniform/(▲⁶; [3x ▲².⬣²]; ⬣³)"             → Tiling.fiveUniformThreeOneOne(8, 8),
        "5-uniform/(▲⁶; [3x ▲².⬣²]; ⬣³)_alt2"        → Tiling.fiveUniformThreeOneOne2(8, 8),
        "5-uniform/([3x ▲⁶]; ▲⁴.⬣; ▲².⬣²)"           → Tiling.fiveUniformThreeOneOne3(8, 8),
        "5-uniform/([3x ▲⁶]; ▲⁴.⬣; ▲².⬣²)_alt"       → Tiling.fiveUniformThreeOneOne4(8, 8),
        "5-uniform/(▲⁶; [3x ⬣³]; ▲².⬣²)"             → Tiling.fiveUniformTwoTwoOne(8, 8),
        "5-uniform/([2x ▲⁶]; ▲⁴.⬣; [2x ▲².⬣²])"      → Tiling.fiveUniformTwoTwoOne2(8, 8),
        "5-uniform/(▲⁶; [2x ▲².⬣²]; [2x ⬣³])"        → Tiling.fiveUniformTwoTwoOne3(8, 8),
        "5-uniform/([2x ▲⁶]; [2x ▲⁴.⬣]; ▲².⬣²)"      → Tiling.fiveUniformTwoTwoOne4(8, 8),
        "5-uniform/(▲³.■²; [2x ▲².⬣²]; [2x ▲.■².⬣])" → Tiling.fiveUniformTwoTwoOne5(12, 12),
        "5-uniform/([4x ▲⁶]; ▲⁴.⬣)"                  → Tiling.fiveUniformFourOne(8, 8),
        "5-uniform/([4x ▲.⬣.▲.⬣]; ▲.■².⬣)"           → Tiling.fiveUniformFourOne2(12, 12),
        "5-uniform/([4x ▲.⬣.▲.⬣]; ▲.■².⬣)_alt"       → Tiling.fiveUniformFourOne3(12, 12),
        "6-uniform/(▲⁶; [4x ⬣³]; ▲².⬣²)"             → Tiling.sixUniformFourOneOne(8, 8)
      )

      val wd = os.pwd / "out"
      os.makeDir.all(wd / "jvm" / "myAlgos" / "regular")
      os.makeDir.all(wd / "jvm" / "myAlgos" / "uniform")
      os.makeDir.all(wd / "jvm" / "myAlgos" / "2-uniform")
      os.makeDir.all(wd / "jvm" / "myAlgos" / "3-uniform")
      os.makeDir.all(wd / "jvm" / "myAlgos" / "4-uniform")
      os.makeDir.all(wd / "jvm" / "myAlgos" / "5-uniform")
      os.makeDir.all(wd / "jvm" / "myAlgos" / "6-uniform")
      m.foreach({
        case (name, t) ⇒
          saveFilePretty(draw(t, labelStyle = 0), "out/jvm/myAlgos/" + name)
          logger.debug(name + " done")
      })
    }

    def test(): Unit = {
      val wd = os.pwd / "out"
      os.makeDir.all(wd / "jvm" / "myTest")
      val t = Tiling.threeUniformOneOneOne8(6, 6)
      saveFilePretty(draw(t, labelStyle = 2, polys = true), "out/jvm/myTest/" + "test1")
      logger.debug("test done")
    }

    def docs(): Unit = {
      val small = Tiling.fromG(
        Graph(
          1 ~ 2,
          2 ~ 3,
          3 ~ 4,
          4 ~ 5,
          5 ~ 1,
          5 ~ 6,
          6 ~ 7,
          7 ~ 8,
          8 ~ 1,
          8 ~ 9,
          9 ~ 10,
          10 ~ 11,
          11 ~ 12,
          12 ~ 13,
          13 ~ 14,
          14 ~ 15,
          15 ~ 2
        ))
      val big = Tiling.fromG(
        Graph(
          102 ~ 106,
          102 ~ 98,
          52 ~ 53,
          52 ~ 127,
          133 ~ 134,
          2 ~ 3,
          2 ~ 19,
          83 ~ 84,
          127 ~ 128,
          127 ~ 123,
          106 ~ 107,
          106 ~ 103,
          98 ~ 38,
          77 ~ 78,
          48 ~ 44,
          48 ~ 51,
          27 ~ 28,
          27 ~ 4,
          121 ~ 116,
          121 ~ 122,
          71 ~ 72,
          42 ~ 24,
          21 ~ 22,
          21 ~ 49,
          123 ~ 52,
          94 ~ 91,
          94 ~ 95,
          73 ~ 69,
          73 ~ 74,
          44 ~ 21,
          23 ~ 10,
          117 ~ 115,
          117 ~ 118,
          88 ~ 89,
          67 ~ 62,
          67 ~ 68,
          38 ~ 39,
          38 ~ 103,
          17 ~ 18,
          92 ~ 36,
          92 ~ 93,
          92 ~ 35,
          34 ~ 91,
          34 ~ 35,
          13 ~ 14,
          5 ~ 6,
          5 ~ 1,
          28 ~ 29,
          28 ~ 11,
          109 ~ 112,
          109 ~ 110,
          109 ~ 105,
          59 ~ 111,
          59 ~ 60,
          9 ~ 10,
          9 ~ 1,
          132 ~ 128,
          103 ~ 98,
          103 ~ 104,
          82 ~ 79,
          82 ~ 83,
          74 ~ 30,
          53 ~ 133,
          53 ~ 54,
          32 ~ 33,
          32 ~ 13,
          24 ~ 23,
          3 ~ 1,
          3 ~ 4,
          126 ~ 123,
          126 ~ 129,
          97 ~ 93,
          97 ~ 98,
          76 ~ 77,
          128 ~ 53,
          99 ~ 97,
          99 ~ 100,
          78 ~ 82,
          78 ~ 74,
          70 ~ 67,
          70 ~ 71,
          49 ~ 44,
          49 ~ 50,
          20 ~ 25,
          20 ~ 3,
          93 ~ 37,
          43 ~ 45,
          43 ~ 44,
          43 ~ 20,
          124 ~ 121,
          124 ~ 125,
          14 ~ 15,
          14 ~ 7,
          118 ~ 119,
          89 ~ 90,
          39 ~ 16,
          39 ~ 40,
          120 ~ 116,
          120 ~ 124,
          10 ~ 2,
          114 ~ 112,
          85 ~ 86,
          85 ~ 81,
          64 ~ 65,
          64 ~ 117,
          35 ~ 14,
          35 ~ 36,
          108 ~ 105,
          79 ~ 74,
          79 ~ 80,
          58 ~ 54,
          29 ~ 73,
          29 ~ 30,
          8 ~ 17,
          8 ~ 9,
          81 ~ 33,
          60 ~ 58,
          31 ~ 32,
          31 ~ 12,
          125 ~ 126,
          104 ~ 105,
          104 ~ 40,
          104 ~ 39,
          75 ~ 73,
          75 ~ 76,
          54 ~ 50,
          25 ~ 26,
          25 ~ 61,
          4 ~ 5,
          4 ~ 11,
          129 ~ 130,
          129 ~ 127,
          19 ~ 43,
          19 ~ 21,
          19 ~ 20,
          100 ~ 101,
          50 ~ 22,
          65 ~ 66,
          15 ~ 16,
          96 ~ 93,
          96 ~ 99,
          46 ~ 115,
          46 ~ 47,
          46 ~ 63,
          119 ~ 120,
          111 ~ 110,
          111 ~ 113,
          90 ~ 86,
          90 ~ 94,
          69 ~ 29,
          61 ~ 62,
          61 ~ 45,
          40 ~ 17,
          40 ~ 41,
          11 ~ 5,
          11 ~ 12,
          134 ~ 54,
          113 ~ 114,
          84 ~ 81,
          84 ~ 87,
          63 ~ 61,
          63 ~ 64,
          115 ~ 63,
          115 ~ 116,
          107 ~ 108,
          86 ~ 34,
          57 ~ 59,
          57 ~ 42,
          36 ~ 37,
          36 ~ 15,
          7 ~ 15,
          7 ~ 8,
          130 ~ 131,
          80 ~ 81,
          80 ~ 32,
          80 ~ 31,
          51 ~ 49,
          51 ~ 52,
          30 ~ 31,
          30 ~ 79,
          1 ~ 2,
          105 ~ 41,
          55 ~ 50,
          55 ~ 58,
          55 ~ 56,
          47 ~ 121,
          47 ~ 48,
          26 ~ 67,
          26 ~ 27,
          122 ~ 123,
          122 ~ 48,
          122 ~ 51,
          101 ~ 102,
          72 ~ 69,
          72 ~ 75,
          22 ~ 23,
          22 ~ 55,
          116 ~ 47,
          95 ~ 96,
          66 ~ 62,
          66 ~ 70,
          45 ~ 25,
          45 ~ 46,
          37 ~ 38,
          37 ~ 97,
          16 ~ 8,
          16 ~ 17,
          68 ~ 28,
          68 ~ 69,
          68 ~ 27,
          18 ~ 24,
          18 ~ 10,
          18 ~ 9,
          112 ~ 108,
          91 ~ 86,
          91 ~ 92,
          62 ~ 26,
          41 ~ 42,
          41 ~ 109,
          33 ~ 85,
          33 ~ 34,
          12 ~ 6,
          12 ~ 13,
          56 ~ 57,
          56 ~ 24,
          56 ~ 23,
          6 ~ 7,
          6 ~ 13,
          87 ~ 85,
          87 ~ 88,
          131 ~ 132,
          110 ~ 57,
          110 ~ 42
        ))
      saveFilePretty(draw(big, labelStyle = 2, polys = true), "docs/" + "(▲.■.⬣.■)")
      saveFilePretty(draw(small, labelStyle = 2, polys = false, perim = false), "docs/" + "(⬟².10)_label")
      saveFilePretty(draw(small, labelStyle = 0, polys = true, perim = false), "docs/" + "(⬟².10)_filled")
      saveFilePretty(draw(small, labelStyle = 0, polys = false, perim = true), "docs/" + "(⬟².10)_perimeter")
      logger.debug("docs done")
    }

    action match {
      case "algos" ⇒ algos()
      case "docs"  ⇒ docs()
      case "test"  ⇒ test()
      case _       ⇒ logger.error("Unknown output action")
    }
  }

}
