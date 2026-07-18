export interface EmailLog {
  id: number;
  recruiterName: string | null;
  recruiterEmail: string;
  subject: string;
  result: 'SUCCESS' | 'FAILURE';
  errorMessage: string | null;
  sentAt: string;
}
