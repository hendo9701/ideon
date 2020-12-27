package common.util

import org.junit.Test

internal class ResourceProviderTest {
  @Test
  fun `getPaths should return default config file when app directory is missing`() {

    assert(ResourceProvider.getPaths().endsWith("config.json"))
  }

}