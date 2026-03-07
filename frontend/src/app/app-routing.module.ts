import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./features/timeline-list/timeline-list.component').then(m => m.TimelineListComponent)
  },
  {
    path: 'timelines/:id',
    loadComponent: () =>
      import('./features/timeline-editor/timeline-editor.component').then(m => m.TimelineEditorComponent)
  },
  {
    path: 'graph',
    loadComponent: () =>
      import('./features/graph-view/graph-view.component').then(m => m.GraphViewComponent)
  },
  { path: '**', redirectTo: '' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
