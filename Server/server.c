#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <pthread.h>

#define CLIENTS_LIMIT 10
#define RUNNING 1
#define NO_FLAG 0
#define TABLE_SIZE 3
#define GRID_SIZE 9

const uint16_t WAIT_FLAG = 0<<8;
const uint16_t START_FLAG = 1<<8;
const uint16_t OPPONENT_MOVE_FLAG = 2<<8;
const uint16_t YOUR_MOVE_FLAG = 3<<8;
const uint16_t WIN_FLAG = 4<<8;
const uint16_t LOSE_FLAG = 5<<8;
const uint16_t DRAW_FLAG = 6<<8;

enum {
    GAME_NOT_OVER,
    PLAYER1_WIN,
    PLAYER2_WIN,
    GAME_DRAW
};

typedef struct player_t {
    uint32_t socket;
    char* name;
    uint16_t name_len;
} player_t;

player_t* create_player(uint32_t socket, char* name, uint16_t name_len) {
    player_t* player = (player_t*)malloc(sizeof(player_t));
    player->socket = socket;
    player->name = name;
    player->name_len = name_len;
    return player;
}

void delete_player(player_t* player) {
    free(player->name);
    free(player);
}

typedef struct game_t {
    player_t* player1;
    player_t* player2;
} game_t;

game_t* create_game(player_t* player1, player_t* player2) {
    game_t* game = (game_t*)malloc(sizeof(game_t));
    game->player1 = player1;
    game->player2 = player2;
    return game;
}

void delete_game(game_t* game) {
    delete_player(game->player1);
    delete_player(game->player2);
    free(game);
}

player_t* receive_player(uint32_t socket) {
    uint16_t name_len;
    recv(socket, &name_len, sizeof(uint16_t), MSG_WAITALL);
    name_len = ntohs(name_len);
    char* name = (char*)malloc((name_len + 1) * sizeof(char));
    recv(socket, name, name_len * sizeof(char), MSG_WAITALL);
    name[name_len] = '\0';

    return create_player(socket, name, name_len);
}

uint8_t row_col(size_t i, size_t j) {
    return TABLE_SIZE * i + j;
}

uint8_t check_win(char* table) {
    if (table[row_col(1, 1)] != ' ') {
        if (table[row_col(0, 0)] == table[row_col(1, 1)] && table[row_col(1, 1)] == table[row_col(2, 2)]) {
            return table[row_col(1, 1)] == 'X' ? PLAYER1_WIN : PLAYER2_WIN;
        }
        if (table[row_col(2, 0)] == table[row_col(1, 1)] && table[row_col(1, 1)] == table[row_col(0, 2)]) {
            return table[row_col(1, 1)] == 'X' ? PLAYER1_WIN : PLAYER2_WIN;
        }
    }
    for (size_t i = 0; i < TABLE_SIZE; ++i) {
        if (table[row_col(i, i)] == ' ') {
            continue;
        }
        if (table[row_col(0, i)] == table[row_col(1, i)] && table[row_col(1, i)] == table[row_col(2, i)]) {
            return table[row_col(i, i)] == 'X' ? PLAYER1_WIN : PLAYER2_WIN;
        }
        if (table[row_col(i, 0)] == table[row_col(i, 1)] && table[row_col(i, 1)] == table[row_col(i, 2)]) {
            return table[row_col(i, i)] == 'X' ? PLAYER1_WIN : PLAYER2_WIN;
        }
    }
    for (size_t i = 0; i < TABLE_SIZE * TABLE_SIZE; ++i) {
        if (table[i] == ' ') {
            return GAME_NOT_OVER;
        }
    }
    return GAME_DRAW;
}

void* game_function(void* arg) {
    game_t* game = (game_t *) arg;
        
    player_t* player1 = game->player1;
    player_t* player2 = game->player2;

    if (rand() & 2) {
        player_t* temp = player1;
        player1 = player2;
        player2 = temp;
    }

    send(player1->socket, &START_FLAG, sizeof(uint16_t), NO_FLAG);
    send(player2->socket, &START_FLAG, sizeof(uint16_t), NO_FLAG);

    uint16_t player1_name_len = htons(player1->name_len);
    uint16_t player2_name_len = htons(player2->name_len);
    send(player2->socket, &player1_name_len, sizeof(uint16_t), NO_FLAG);
    send(player1->socket, &player2_name_len, sizeof(uint16_t), NO_FLAG);
    send(player2->socket, player1->name, player1->name_len, NO_FLAG);
    send(player1->socket, player2->name, player2->name_len, NO_FLAG);

    char X = 'X', O = '0';
    send(player1->socket, &X, sizeof(char), NO_FLAG);
    send(player2->socket, &O, sizeof(char), NO_FLAG);

    char table[GRID_SIZE];
    for (size_t i = 0; i < GRID_SIZE; ++i) {
        table[i] = ' ';
    }

    do {
        uint8_t win_flag;
        uint16_t move;
        send(player1->socket, &YOUR_MOVE_FLAG, sizeof(uint16_t), NO_FLAG);
        send(player1->socket, table, GRID_SIZE * sizeof(char), NO_FLAG);
        send(player2->socket, &OPPONENT_MOVE_FLAG, sizeof(uint16_t), NO_FLAG);

        recv(player1->socket, &move, sizeof(uint16_t), MSG_WAITALL);
        move = ntohs(move);
        table[move] = 'X';
        win_flag = check_win(table);
        
        if (win_flag == GAME_DRAW) {
            send(player1->socket, &DRAW_FLAG, sizeof(uint16_t), NO_FLAG);
            send(player1->socket, table, GRID_SIZE * sizeof(char), NO_FLAG);
            send(player2->socket, &DRAW_FLAG, sizeof(uint16_t), NO_FLAG);
            send(player2->socket, table, GRID_SIZE * sizeof(char), NO_FLAG);
            break;
        }
        else if (win_flag == PLAYER1_WIN) {
            send(player1->socket, &WIN_FLAG, sizeof(uint16_t), NO_FLAG);
            send(player1->socket, table, GRID_SIZE * sizeof(char), NO_FLAG);
            send(player2->socket, &LOSE_FLAG, sizeof(uint16_t), NO_FLAG);
            send(player2->socket, table, GRID_SIZE * sizeof(char), NO_FLAG);
            break;
        }
        else if (win_flag == PLAYER2_WIN) {
            send(player1->socket, &LOSE_FLAG, sizeof(uint16_t), NO_FLAG);
            send(player1->socket, table, GRID_SIZE * sizeof(char), NO_FLAG);
            send(player2->socket, &WIN_FLAG, sizeof(uint16_t), NO_FLAG);
            send(player2->socket, table, GRID_SIZE * sizeof(char), NO_FLAG);
            break;
        }

        send(player1->socket, &OPPONENT_MOVE_FLAG, sizeof(uint16_t), NO_FLAG);
        send(player2->socket, &YOUR_MOVE_FLAG, sizeof(uint16_t), NO_FLAG);
        send(player2->socket, table, GRID_SIZE * sizeof(char), NO_FLAG);

        recv(player2->socket, &move, sizeof(uint16_t), MSG_WAITALL);
        move = ntohs(move);
        table[move] = '0';
        win_flag = check_win(table);

        if (win_flag == GAME_DRAW) {
            send(player1->socket, &DRAW_FLAG, sizeof(uint16_t), NO_FLAG);
            send(player1->socket, table, GRID_SIZE * sizeof(char), NO_FLAG);
            send(player2->socket, &DRAW_FLAG, sizeof(uint16_t), NO_FLAG);
            send(player2->socket, table, GRID_SIZE * sizeof(char), NO_FLAG);
            break;
        }
        else if (win_flag == PLAYER1_WIN) {
            send(player1->socket, &WIN_FLAG, sizeof(uint16_t), NO_FLAG);
            send(player1->socket, table, GRID_SIZE * sizeof(char), NO_FLAG);
            send(player2->socket, &LOSE_FLAG, sizeof(uint16_t), NO_FLAG);
            send(player2->socket, table, GRID_SIZE * sizeof(char), NO_FLAG);
            break;
        }
        else if (win_flag == PLAYER2_WIN) {
            send(player1->socket, &LOSE_FLAG, sizeof(uint16_t), NO_FLAG);
            send(player1->socket, table, GRID_SIZE * sizeof(char), NO_FLAG);
            send(player2->socket, &WIN_FLAG, sizeof(uint16_t), NO_FLAG);
            send(player2->socket, table, GRID_SIZE * sizeof(char), NO_FLAG);
            break;
        }
    } while(RUNNING);

    delete_game(game);
    return NULL;
}

int main(int argc, char* argv[]) {
    uint32_t server_socket = socket(AF_INET, SOCK_STREAM, 0);
    if (server_socket < 0) {
        perror("Socket error");
        return EXIT_FAILURE;
    }

    struct sockaddr_in server;
    memset(&server, 0, sizeof(server));
    server.sin_port = htons(4004);
    server.sin_family = AF_INET;
    server.sin_addr.s_addr = INADDR_ANY;

    if (bind(server_socket, (struct sockaddr *) &server, sizeof(server)) < 0) {
        perror("Bind error");
        return EXIT_FAILURE;
    }

    listen(server_socket, CLIENTS_LIMIT);
    
    struct sockaddr_in player1_struct, player2_struct;
    uint32_t len_struct = sizeof(struct sockaddr_in);
    
    memset(&player1_struct, 0, len_struct);
    memset(&player2_struct, 0, len_struct);

    do {
        uint32_t player1_socket = accept(server_socket, (struct sockaddr *) &player1_struct, &len_struct);
        player_t* player1 = receive_player(player1_socket);
        send(player1->socket, &WAIT_FLAG, sizeof(uint16_t), 0);

        uint32_t player2_socket = accept(server_socket, (struct sockaddr *) &player2_struct, &len_struct);
        player_t* player2 = receive_player(player2_socket);

        pthread_t game_thread;
        pthread_create(&game_thread, NULL, game_function, create_game(player1, player2));
    } while(RUNNING);

    return EXIT_SUCCESS;
}
