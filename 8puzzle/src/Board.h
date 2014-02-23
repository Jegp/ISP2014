#ifndef BOARD_H
#define BOARD_H

#include <algorithm>
#include <vector>
#include <stdlib.h>
#include <SDL/SDL.h>

struct Action {
  int x1,y1,x2,y2;
  Action(int x1, int y1, int x2, int y2) 
  : x1(x1), y1(y1), x2(x2), y2(y2) {}
};

struct Node;

struct Board { 

  SDL_Surface* text;
  TTF_Font* font;
  int fields[3][3];

  Board() : font(TTF_OpenFont("sans-serif", 40)) { 
    for (int x = 0; x < 3; x++)
      for (int y = 0; y < 3; y++)
	fields[x][y] = x + y * 3;
  }

  Board(int state[3][3]) : font(TTF_OpenFont("sans-serif", 40)) { 
    std::copy(&state[0][0], &state[3][3], &fields[0][0]);
  }

  std::vector<Action>* actions(int** state); 

  bool goal(int state[3][3]);

  void moveMouse(int x, int y);
  
  void moveField(int x, int y);

  void render(SDL_Surface* screen);

  static void result(int state[3][3], int copy[3][3], Action action);

  std::vector<Action>* search();

  void solution(Node* node, std::vector<Action>* actions);

  static void swap(int state[3][3], Action action);

};

struct Node {
  int state[3][3];
  Node* parent;
  Action* action;
  Node(int newState[3][3]) : action(NULL), parent(NULL) {
    std::copy(&newState[0][0], &newState[3][3], &state[0][0]);
  }
  Node(Node* parent, Action* action)
  : parent(parent), action(action) {
    Board::result(parent->state, state, *action);
  }
};


#endif
