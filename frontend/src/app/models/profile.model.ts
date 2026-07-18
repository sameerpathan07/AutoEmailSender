export interface Profile {
  id: number;
  fullName: string | null;
  resumeFileName: string | null;
  resumeContentType: string | null;
  defaultSubject: string | null;
  defaultDescription: string | null;
  senderEmail: string | null;
  followUpSubject: string | null;
  followUpDescription: string | null;
}
