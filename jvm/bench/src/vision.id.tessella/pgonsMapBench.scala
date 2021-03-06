package vision.id.tessella

import org.scalameter._
import org.scalatest.FlatSpec

import vision.id.tessella.Alias.Tiling

class pgonsMapBench extends FlatSpec with TilingUtils {

  "Method pgonsMap" must "must execute in less than 6 seconds" in {
    val time = config(
      Key.exec.benchRuns -> 5,
      Key.verbose        -> true
    ) measure {
//    ) withWarmer {
//      new Warmer.Default
//    } withMeasurer {
//      new Measurer.IgnoringGC
//    } measure {
      Tiling.threeUniformOneOneOne8(6, 6).pgonsMap
    }
    assert(time.value < 7000.0)

  }

}
