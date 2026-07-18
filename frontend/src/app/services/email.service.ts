import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { EmailLog } from '../models/email-log.model';

const BASE_URL = 'http://localhost:8080/api/email';

export interface SendEmailRequest {
  name?: string;
  email: string;
  subject?: string;
  description?: string;
  force?: boolean;
}

// status: SUCCESS | FAILURE | DUPLICATE (already contacted) | LIMIT_REACHED (daily cap hit)
export interface SendEmailResponse {
  status: 'SUCCESS' | 'FAILURE' | 'DUPLICATE' | 'LIMIT_REACHED';
  error?: string;
  lastSentAt?: string;
  sentCountSoFar?: number;
}

export interface QuotaStatus {
  dailyLimit: number;
  sentToday: number;
  remaining: number;
}

@Injectable({ providedIn: 'root' })
export class EmailService {
  constructor(private http: HttpClient) {}

  sendSingle(request: SendEmailRequest): Observable<SendEmailResponse> {
    return this.http.post<SendEmailResponse>(`${BASE_URL}/send`, request);
  }

  getLogs(): Observable<EmailLog[]> {
    return this.http.get<EmailLog[]>(`${BASE_URL}/logs`);
  }

  getQuota(): Observable<QuotaStatus> {
    return this.http.get<QuotaStatus>(`${BASE_URL}/quota`);
  }
}
