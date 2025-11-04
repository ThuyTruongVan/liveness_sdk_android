# Sets the minimum version of CMake required to build your native library.
# This ensures that a certain set of CMake features is available to
# your build.

cmake_minimum_required(VERSION 3.4.1)

# Specifies a library name, specifies whether the library is STATIC or
# SHARED, and provides relative paths to the source code. You can
# define multiple libraries by adding multiple add_library() commands,
# and CMake builds them for you. When you build your app, Gradle
# automatically packages shared libraries with your APK.

add_library( # Specifies the name of the library.
             secrets

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             secrets.cpp )
#(4K)
#add_library(secrets4k SHARED secrets.cpp)
#target_link_libraries(secrets4k log)

# 16K (aarch64)
if (CMAKE_SYSTEM_PROCESSOR MATCHES "aarch64")
    target_link_options(secrets PRIVATE
            "-Wl,-z,max-page-size=16384"
            "-Wl,-z,common-page-size=4096"
    )
endif()
if (CMAKE_SYSTEM_PROCESSOR MATCHES "x86_64")
    target_link_options(secrets PRIVATE
            "-Wl,-z,max-page-size=16384"
            "-Wl,-z,common-page-size=4096"
    )
endif()