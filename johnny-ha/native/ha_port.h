// SPDX-License-Identifier: GPL-3.0-or-later
#ifndef HA_PORT_H
#define HA_PORT_H
#include <stddef.h>
#include <stdio.h>
#include <SDL.h>
void *ha_malloc(size_t);
void ha_free(void *);
FILE *ha_fopen(const char *, const char *);
int ha_fclose(FILE *);
SDL_Surface *ha_surface(Uint32,int,int,int,Uint32,Uint32,Uint32,Uint32);
SDL_Surface *ha_surface_from(void *,int,int,int,int,Uint32,Uint32,Uint32,Uint32);
void ha_free_surface(SDL_Surface *);
void ha_check_stop(void);
void ha_fail(const char *);
int ha_night(void);
int ha_should_preempt(void);
int ha_fan_pending(void);
int ha_take_fan_request(void);
void ha_fan_started(void);
void ha_fan_finished(void);
void ha_present(SDL_Surface *);
void ha_wait(unsigned);
#endif
