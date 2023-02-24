import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {HttpClientModule} from '@angular/common/http';
import {RouterModule, Routes} from '@angular/router';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';

import {SectorsEditComponent} from './sectors-edit.component';
import {AppComponent} from './app-component';

const routes: Routes = [
  {path: '', component: SectorsEditComponent},
];

@NgModule({
  declarations: [
    AppComponent,
    SectorsEditComponent
  ],
  imports: [
    BrowserModule,
    RouterModule.forRoot(routes),
    HttpClientModule,
    FormsModule,
    ReactiveFormsModule
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}
