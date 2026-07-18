import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Profile } from '../models/profile.model';

const BASE_URL = 'http://localhost:8080/api/profile';

@Injectable({ providedIn: 'root' })
export class ProfileService {
  constructor(private http: HttpClient) {}

  getProfile(): Observable<Profile> {
    return this.http.get<Profile>(BASE_URL);
  }

  updateDetails(details: {
    fullName: string;
    defaultSubject: string;
    defaultDescription: string;
    senderEmail: string;
    followUpSubject: string;
    followUpDescription: string;
  }): Observable<Profile> {
    return this.http.put<Profile>(BASE_URL, details);
  }

  uploadResume(file: File): Observable<Profile> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<Profile>(`${BASE_URL}/resume`, formData);
  }
}
