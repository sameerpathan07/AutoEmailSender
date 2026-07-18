export interface Recruiter {
  id: number;
  name: string | null;
  email: string;
  status: 'PENDING' | 'SENT' | 'FOLLOWED_UP' | 'FAILED';
  createdAt: string;
  lastSentAt: string | null;
  sentCount: number;
}
