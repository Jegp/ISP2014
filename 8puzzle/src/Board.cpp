#include <SDL/SDL.h>
#include <SDL/SDL_ttf.h>
#include <stdio.h>
#include <vector>
#include <queue>

#include "Board.h"

std::vector<Action>* Board::actions(int** state) {
    int ex, ey;
    for (int x = 0; x <= 2; x++) {
      for (int y = 0; y <= 2; y++) {
	if (state[x][y] == 0) {
	  ex = x;
	  ey = y;
	  break;
	}
      }
    }
    
    std::vector<Action>* actions;

    // Examine for vacant horizontal spot
    if (ex > 0 && state[ex - 1][ey] == 0) 
      actions->push_back(Action(ex, ey, ex - 1, ey));
    if (ex < 2 && state[ex + 1][ey] == 0) 
      actions->push_back(Action(ex, ey, ex + 1, ey));
    
    // Look for vacant vertical slot
    if (ey > 0 && state[ex][ey - 1] == 0) 
      actions->push_back(Action(ex, ey, ex, ey - 1));
    if (ey < 2 && state[ex][ey + 1] == 0) 
      actions->push_back(Action(ex, ey, ex, ey + 1));
    
    return actions;
}

bool Board::goal(int state[3][3]) {
  if (state[0][0] != 0) return false;
  for (int x = 0; x <= 2; x++) {
    for (int y = 0; y <= 2; y++) {
      if (x == 2 && y == 2) continue;
      if (x == 2 && y < 2 
	  && state[x][y] != state[0][y + 1] - 1) return false;
      if (x < 2 && state[x][y] != state[x + 1][y] - 1) return false;
    }
  }
  return true;
}

void Board::swap(int state[3][3], Action action) {
  int tmp = state[action.x2][action.y2];
  state[action.x2][action.y2] = state[action.x1][action.y1];
  state[action.x1][action.y1] = tmp;
}

void Board::moveMouse(int x, int y) {
  moveField(x / 100, y / 100);
}

void Board::moveField(int x, int y) {
  // Examine for vacant horizontal spot
  if (x > 0 && fields[x - 1][y] == 0) 
    swap(fields, Action(x, y, x - 1, y));
  if (x < 2 && fields[x + 1][y] == 0) 
    swap(fields, Action(x, y, x + 1, y));

  // Look for vacant vertical slot
  if (y > 0 && fields[x][y - 1] == 0) 
    swap(fields, Action(x, y, x, y - 1));
  if (y < 2 && fields[x][y + 1] == 0) 
    swap(fields, Action(x, y, x, y + 1));

  printf("Goal? %d\n", goal(fields));
}

void Board::render(SDL_Surface* screen) {

  int color_background = SDL_MapRGB(screen->format, 200, 200, 200);
  SDL_Color color = {0x00, 0x00, 0x00, 0xff};

  // Draw text
  SDL_Rect rect;

  char* content;

  for (int x = 0; x < 3; x++) {
    for (int y = 0; y < 3; y++) {
      int square_x = 100 * x + 30;
      int square_y = 100 * y + 30;
      int value = fields[x][y];
      if (value > 0) {
	sprintf(content, "%d", value);
	rect = (SDL_Rect) { square_x - 20, square_y - 20, 70, 80 };
	SDL_FillRect(screen, &rect, color_background);
      } else {
	content[0] = '\0';
      }
      text = TTF_RenderText_Solid(font, content, color);
      rect = (SDL_Rect) { square_x, square_y, 50, 50};
      SDL_BlitSurface(text, NULL, screen, &rect);
    }
  }

  // Free memory
  SDL_FreeSurface(text);
  
}

void Board::result(int state[3][3], int copy[3][3], Action action) {
  for (int x = 0; x <= 2; x++) {
    for (int y = 0; y <= 2; y++) {
      copy[x][y] = state[x][y];
    }
  }
  
  swap(copy, action);
}

bool equals(int** state1, int** state2) {
  for (int x = 0; x <= 2; x++) {
    for (int y = 0; y <= 2; y++) {
      if (state1[x][y] != state2[x][y]) return false;
    }
  }
  return true;
}

void print(int w[3][3]) {
  printf("%d %d %d\n", w[0][0], w[1][0], w[2][0]);
  printf("%d %d %d\n", w[0][1], w[1][1], w[2][1]);
  printf("%d %d %d\n", w[0][2], w[1][2], w[2][2]);
}

std::vector<Action>* Board::search() {
  Node* initial = new Node(fields);
  std::vector<Action>* resultingActions;

  // If we have a goal state, return the solution
  if (goal(initial->state)) {
    solution(initial, resultingActions);
    return resultingActions;
  }

  // If we don't have a goal state, go explore
  std::queue<Node*> frontier;
  frontier.push(initial);
  std::queue<int**> explored;

  while (true) {
    if (frontier.empty()) return NULL;
    
    Node* current = frontier.front();
    frontier.pop();

    explored.push((int**) current->state);
    
    std::vector<Action>* problems = actions((int**) current->state);
    for (int n = 0; n < problems->size(); n++) {
      bool contains = false;
      Node* child = new Node(current, &problems->at(n));
      int size = explored.size();
      for (int n = 0; n < size; n++) {
	int** someNode = explored.front();
	if (equals(someNode, (int**) child->state)) {
	  explored.pop();
	  explored.push(someNode);
	  contains = true;
	  break;
	}
      }

      for (int n = 0; n < frontier.size(); n++) {
	Node* frontierNode = frontier.front();
	frontier.pop();
	frontier.push(frontierNode);
	if (equals((int**) child->state, (int**) frontierNode->state)) {
	  contains = true;
	}	
      }
      
      if (!contains) {
	if (goal(child->state)) {
	  solution(child, resultingActions);
	  return resultingActions;
	}
	frontier.push(child);
      }
    }
    
  }

  return NULL;
}

void Board::solution(Node* node, std::vector<Action>* actions) {
  if (node == NULL) return;
  actions->push_back(*node->action);
  solution(node->parent, actions);
}
