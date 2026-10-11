package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.PresetAvatar
import com.example.data.model.TalkingVideoProject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("PhotoParle", appName)
  }

  @Test
  fun `verify preset avatars exist`() {
    assertTrue(PresetAvatar.ALL.isNotEmpty())
    val anchor = PresetAvatar.ALL.first()
    assertNotNull(anchor.name)
    assertNotNull(anchor.defaultGreeting)
  }

  @Test
  fun `verify talking video project creation`() {
    val project = TalkingVideoProject(
      title = "Test Video",
      avatarSource = "res:anchor",
      scriptText = "Bonjour le monde",
      voiceTone = "Sérieux",
      speechRate = 1.0f,
      speechPitch = 1.0f
    )
    assertEquals("Test Video", project.title)
    assertEquals("Bonjour le monde", project.scriptText)
  }
}
