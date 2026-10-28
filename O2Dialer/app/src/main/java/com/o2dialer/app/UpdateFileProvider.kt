package com.o2dialer.app

import androidx.core.content.FileProvider

/** Dedicated provider avoids OEM issues with declaring the library class directly. */
class UpdateFileProvider : FileProvider()
