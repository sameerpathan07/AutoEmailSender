import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Recruiter } from '../models/recruiter.model';

const BASE_URL = 'https://autoemailsender-0cje.onrender.com/api/recruiters';

export interface BulkSendResult {
  totalAttempted: number;
  successCount: number;
  failureCount: number;
  failedEmails: string[];
  remainingInQueue: number;
  limitReached: boolean;
}

export interface UploadResult {
  addedCount: number;
  duplicateCount: number;
  added: Recruiter[];
  duplicateEmails: string[];
}

@Injectable({ providedIn: 'root' })
export class RecruiterService {
  constructor(private http: HttpClient) {}

  getAll(): Observable<Recruiter[]> {
    return this.http.get<Recruiter[]>(BASE_URL);
  }

  uploadExcel(file: File): Observable<UploadResult> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<UploadResult>(`${BASE_URL}/upload-excel`, formData);
  }

  uploadText(text: string): Observable<UploadResult> {
    return this.http.post<UploadResult>(`${BASE_URL}/upload-text`, { text });
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${BASE_URL}/${id}`);
  }

  sendAllPending(): Observable<BulkSendResult> {
    return this.http.post<BulkSendResult>(`${BASE_URL}/send-all`, {});
  }

  // Normally runs automatically on the backend once a day - exposed here too
  // in case you want to trigger a follow-up round manually.
  sendFollowUpsNow(): Observable<BulkSendResult> {
    return this.http.post<BulkSendResult>(`${BASE_URL}/send-followups`, {});
  }
}
