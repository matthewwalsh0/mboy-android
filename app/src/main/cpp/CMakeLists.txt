cmake_minimum_required(VERSION 3.4.1)
set(CMAKE_CXX_STANDARD 14)

# mboy-core
set(lib_src_DIR core)
set(lib_build_DIR core/build/android)
include_directories( ${lib_src_DIR}/include )
file(MAKE_DIRECTORY ${lib_build_DIR})
add_subdirectory(${lib_src_DIR} ${lib_build_DIR})
add_library( libmboy_core STATIC IMPORTED )
set_target_properties( libmboy_core PROPERTIES IMPORTED_LOCATION ${lib_build_DIR}/libmboy_core.a )

add_library(native-lib SHARED native-lib.cpp AndroidGUI.cpp)
target_link_libraries(native-lib libmboy_core)

find_package(oboe REQUIRED CONFIG)
find_library(log-lib log)

target_link_libraries(
        native-lib
        android
        jnigraphics
        ${log-lib}
        oboe::oboe
)