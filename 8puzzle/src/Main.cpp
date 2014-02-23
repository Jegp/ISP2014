#include <stdio.h>
#include <stdlib.h>
#include <SDL/SDL.h>
#include <SDL/SDL_ttf.h>
#include <SDL/SDL_mixer.h>
#include <emscripten.h>

#include "Board.h"

SDL_Surface *screen;
Board* board;

void main_loop() {
  SDL_Event event;

  while (SDL_PollEvent(&event)) {
    switch (event.type) {
      case SDL_MOUSEBUTTONDOWN: {
	int x, y;
	SDL_GetMouseState(&x, &y);
	board->moveMouse(x, y);
      }
      default: ; break;
    }
  }

  // Clears the screen
  SDL_FillRect(screen, NULL, SDL_MapRGB(screen->format, 255, 255, 255));
  
  // fill stuff
  board->render(screen);
}

int main(int argc, char **argv) {
#if EMSCRIPTEN
  // include GL stuff, to check that we can compile hybrid 2d/GL apps
  extern void glBegin(int mode);
  extern void glBindBuffer(int target, int buffer);
  if (argc == 9876) {
    glBegin(0);
    glBindBuffer(0, 0);
  }

  // init fonts
  SDL_Init(SDL_INIT_VIDEO);
  screen = SDL_SetVideoMode(600, 450, 32, SDL_HWSURFACE);

  printf("Init: %d\n", TTF_Init());

  // Create the game
  int state[3][3] = {{3, 0, 6}, {1, 4, 7}, {2, 5, 8}};
  board = new Board(state);
  board->search();

  printf("Done\n");
  
  // Start main loop
  emscripten_set_main_loop(main_loop, 10, 1);
#endif
  return 0;
}

