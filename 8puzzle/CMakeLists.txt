PROJECT(dogfight)

set(EXECUTABLE_OUTPUT_PATH ${PROJECT_BINARY_DIRS})
set(SRC_DIR ${PROJECT_SOURCE_DIR}/src)

include_directories (
	${PROJECT_BINARY_DIR}
)

#SET(CMAKE_CXX_FLAGS "-Wall -s ASM_JS=1 -O2")

file(GLOB Sources ${SRC_DIR}/*.cpp)
add_executable(eight ${Sources})

target_link_libraries (
	eight
)
