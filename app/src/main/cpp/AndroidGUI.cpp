//
// Created by matthew on 06/07/2020.
//

#include <stdexcept>
#include <android/native_window_jni.h>
#include <android/log.h>
#include <__bit_reference>
#include "AndroidGUI.h"
#include "../../../../../../Library/Android/sdk/ndk/21.0.6113669/toolchains/llvm/prebuilt/darwin-x86_64/sysroot/usr/include/android/native_window.h"
#include "GPU.h"

void AndroidGUI::displayBuffer(uint32 *pixels) {
    std::chrono::steady_clock::time_point end = std::chrono::steady_clock::now();
    uint32 duration = std::chrono::duration_cast<std::chrono::milliseconds>(end - lastFrame).count();

    if(duration < 16) {
        return;
    }

    lastFrame = std::chrono::steady_clock::now();

    ANativeWindow *window = ANativeWindow_fromSurface(env, surfaceView);

    if (NULL == window) {
        throw std::invalid_argument("unable to get native window");
        return;
    }

    int32_t result = ANativeWindow_setBuffersGeometry(window, SCREEN_WIDTH, SCREEN_HEIGHT, WINDOW_FORMAT_RGBA_8888);

    if (result < 0) {
        throw std::invalid_argument("unable to set buffers geometry");
    }

    ANativeWindow_acquire(window);

    ANativeWindow_Buffer buffer;
    auto result2 = ANativeWindow_lock(window, &buffer, NULL);

    if(result2 < 0) {
        throw std::invalid_argument("unable to lock native window");
    }

    memcpy(buffer.bits, pixels, SCREEN_WIDTH * SCREEN_HEIGHT * sizeof(uint32));

    ANativeWindow_unlockAndPost(window);
    ANativeWindow_release(window);

    renderFrameCount += 1;
    std::chrono::steady_clock::time_point renderEnd = std::chrono::steady_clock::now();
    uint32 renderDuration = std::chrono::duration_cast<std::chrono::milliseconds>(end - renderTimer).count();

    if(renderDuration > 1000) {
        renderFps = renderFrameCount;
        renderFrameCount = 0;
        renderTimer = std::chrono::steady_clock::now();
    }
}

void AndroidGUI::displayFPS(uint16 fps) {
    this->fps = fps;
}

AndroidGUI::AndroidGUI(JNIEnv *env, jobject surfaceView) {
    this->env = env;
    this->surfaceView = surfaceView;
}
