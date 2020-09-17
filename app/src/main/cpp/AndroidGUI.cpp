#include "AndroidGUI.h"

#include <stdexcept>

#include "android/native_window_jni.h"
#include "android/log.h"
#include "__bit_reference"
#include "android/native_window.h"
#include "GPU.h"
#include "APU.h"

void AndroidGUI::displayBuffer(u_int32_t *pixels) {
    memcpy(this->pixels, pixels, SCREEN_WIDTH * SCREEN_HEIGHT * sizeof(u_int32_t));
}

void AndroidGUI::displayFPS(u_int16_t fps) {
    this->fps = fps;
}

AndroidGUI::AndroidGUI(JNIEnv *env) {
    this->env = env;

    oboe::AudioStreamBuilder builder;
    builder.setChannelCount(1);
    builder.setFormat(oboe::AudioFormat::Float);
    builder.setSampleRate(SAMPLE_RATE);
    oboe::Result result = builder.openManagedStream(managedStream);

    if (result != oboe::Result::OK) {
        //LOGE("Failed to create stream. Error: %s", oboe::convertToText(result));
    }

    managedStream->requestStart();
}

void AndroidGUI::setSurface(jobject surfaceView) {
    this->surfaceView = surfaceView;
}

void AndroidGUI::playAudio(float *samples, u_int16_t count) {
    managedStream->write(samples, count, 1000000000);
}

void AndroidGUI::render(JNIEnv* env) {
    if(!surfaceView) return;

    ANativeWindow *window = ANativeWindow_fromSurface(env, surfaceView);

    if (NULL == window) {
        return;
    }

    int32_t result = ANativeWindow_setBuffersGeometry(window, SCREEN_WIDTH, SCREEN_HEIGHT, WINDOW_FORMAT_RGBA_8888);

    if (result < 0) {
        return;
    }

    ANativeWindow_acquire(window);

    ANativeWindow_Buffer buffer;
    auto result2 = ANativeWindow_lock(window, &buffer, NULL);

    if(result2 < 0) {
        return;
    }

    memcpy(buffer.bits, this->pixels, SCREEN_WIDTH * SCREEN_HEIGHT * sizeof(u_int32_t));

    ANativeWindow_unlockAndPost(window);
    ANativeWindow_release(window);

    renderFrameCount += 1;
    std::chrono::steady_clock::time_point renderEnd = std::chrono::steady_clock::now();
    u_int32_t renderDuration = std::chrono::duration_cast<std::chrono::milliseconds>(renderEnd - renderTimer).count();

    if(renderDuration > 1000) {
        renderFps = renderFrameCount;
        renderFrameCount = 0;
        renderTimer = std::chrono::steady_clock::now();
    }
}

bool AndroidGUI::isDown(u_int8_t button) {
    return buttonsDown[button];
}

bool AndroidGUI::isOpen() {
    return running;
}

void AndroidGUI::setButtonState(u_int8_t button, bool state) {
    buttonsDown[button] = state;
}
