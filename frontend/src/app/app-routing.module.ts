import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { authGuard } from './infrastructure/auth/auth.guard';

const routes: Routes = [
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./features/auth/register/register.component').then(m => m.RegisterComponent)
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/timeline-list/timeline-list.component').then(m => m.TimelineListComponent)
  },
  {
    path: 'timelines/:id',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/timeline-editor/timeline-editor.component').then(m => m.TimelineEditorComponent)
  },
  {
    path: 'graph',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/graph-view/graph-view.component').then(m => m.GraphViewComponent)
  },
  {
    path: 'stories',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/story-engine/story-list/story-list.component').then(m => m.StoryListComponent)
  },
  {
    path: 'stories/:id/build',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/story-engine/story-builder/story-builder.component').then(m => m.StoryBuilderComponent)
  },
  {
    path: 'stories/:id/play',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/story-engine/story-player/story-player.component').then(m => m.StoryPlayerComponent)
  },
  {
    path: 'stories/:id/graph',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/story-engine/story-graph/story-graph.component').then(m => m.StoryGraphComponent)
  },
  { path: '**', redirectTo: '' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
