cmake_minimum_required(VERSION 3.4.1)
set(CMAKE_CXX_STANDARD 17)

add_subdirectory(deps/mboy-core)
find_package(oboe REQUIRED CONFIG)
find_library(log-lib log)

add_library(native-lib SHARED native-lib.cpp AndroidGUI.cpp)

target_link_libraries(
    native-lib
    android
    jnigraphics
    ${log-lib}
    mboy_core
    oboe::oboe
)