// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.google.devtools.ksp) apply false
  alias(libs.plugins.roborazzi) apply false
  alias(libs.plugins.secrets) apply false
  alias(libs.plugins.google.services) apply false
}

// Automatically ensure debug.keystore exists before any subproject evaluates
val debugKeystore = file("debug.keystore")
if (!debugKeystore.exists()) {
  val base64File = file("debug.keystore.base64")
  if (base64File.exists()) {
    try {
      val decoded = java.util.Base64.getMimeDecoder().decode(base64File.readText().trim())
      debugKeystore.writeBytes(decoded)
    } catch (_: Exception) {}
  }
}
