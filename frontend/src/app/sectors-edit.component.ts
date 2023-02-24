import {Component, OnInit} from '@angular/core';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {v4 as uuid} from 'uuid';

import {UserData, Sector} from './model';

@Component({
  selector: 'app-client-edit',
  templateUrl: './sectors-edit.component.html',
})
export class SectorsEditComponent implements OnInit {

  formGroup: FormGroup;
  data?: UserData;
  sectors: Observable<Sector[]>;
  error?: string;

  constructor(
      private readonly httpClient: HttpClient) {
  }

  ngOnInit(): void {
    this.loadUserData();
    this.sectors = this.getSectors();
    this.formGroup = new FormGroup({
      tos: new FormControl(false, Validators.requiredTrue),
      name: new FormControl(undefined, Validators.required),
      sectors: new FormControl([], Validators.required)
    });
  }

  saveSectors(): void {
    this.httpClient.put('/api/user-data', this.data)
        .subscribe({
          next: () => {
            this.error = undefined;
          },
          error: err => this.error = err.error.error || 'Unknown error'
        });
  }

  private loadUserData() {
    return this.fetchUserData(this.loadSessionId());
  }

  private loadSessionId() {
    const id = localStorage.getItem('sessionId');
    if (id) {
      return id;
    }
    const newId = uuid();
    localStorage.setItem('sessionId', newId);
    return newId;
  }

  private fetchUserData(sessionId: string): void {
    this.httpClient.get<UserData>(`/api/user-data/${sessionId}`)
        .subscribe({
          next: data => this.data = data
        });
  }

  private getSectors(): Observable<Sector[]> {
    return this.httpClient.get<Sector[]>(`/api/sector`);
  }

  get formName() {
    return this.formGroup.get('name')!;
  }

  get formSectors() {
    return this.formGroup.get('sectors')!;
  }

  get formTos() {
    return this.formGroup.get('tos')!;
  }
}
