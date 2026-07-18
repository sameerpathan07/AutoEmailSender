import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { EmailService, SendEmailResponse, QuotaStatus } from '../../services/email.service';
import { RecruiterService, BulkSendResult, UploadResult } from '../../services/recruiter.service';
import { Recruiter } from '../../models/recruiter.model';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit {

  // ----- Daily quota -----
  quota: QuotaStatus | null = null;

  // ----- Single send form -----
  singleName = '';
  singleEmail = '';
  sending = false;
  sendResult: SendEmailResponse | null = null;

  // ----- Bulk upload -----
  pastedText = '';
  selectedFile: File | null = null;
  uploading = false;
  uploadResult: UploadResult | null = null;
  uploadError = '';

  // ----- Recruiter list / bulk send -----
  recruiters: Recruiter[] = [];
  bulkSending = false;
  bulkResult: BulkSendResult | null = null;

  constructor(
    private emailService: EmailService,
    private recruiterService: RecruiterService
  ) {}

  ngOnInit(): void {
    this.refreshRecruiters();
    this.refreshQuota();
  }

  refreshQuota(): void {
    this.emailService.getQuota().subscribe(q => this.quota = q);
  }

  sendSingleEmail(force: boolean = false): void {
    if (!this.singleEmail) return;
    this.sending = true;
    if (!force) this.sendResult = null;

    this.emailService.sendSingle({ name: this.singleName, email: this.singleEmail, force })
      .subscribe({
        next: (res) => {
          this.sendResult = res;
          this.sending = false;
          if (res.status === 'SUCCESS') {
            this.singleName = '';
            this.singleEmail = '';
            this.refreshRecruiters();
            this.refreshQuota();
          }
        },
        error: () => {
          this.sendResult = { status: 'FAILURE', error: 'Request failed. Check backend connection.' };
          this.sending = false;
        }
      });
  }

  // Called from the "Send anyway" button when a DUPLICATE warning is shown.
  confirmSendAnyway(): void {
    this.sendSingleEmail(true);
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedFile = input.files && input.files.length ? input.files[0] : null;
  }

  uploadExcelFile(): void {
    if (!this.selectedFile) return;
    this.uploading = true;
    this.uploadError = '';
    this.recruiterService.uploadExcel(this.selectedFile).subscribe({
      next: (result) => {
        this.uploadResult = result;
        this.uploading = false;
        this.selectedFile = null;
        this.refreshRecruiters();
      },
      error: () => {
        this.uploadError = 'Excel upload failed. Please check the file format.';
        this.uploading = false;
      }
    });
  }

  uploadPastedText(): void {
    if (!this.pastedText.trim()) return;
    this.uploading = true;
    this.uploadError = '';
    this.recruiterService.uploadText(this.pastedText).subscribe({
      next: (result) => {
        this.uploadResult = result;
        this.uploading = false;
        this.pastedText = '';
        this.refreshRecruiters();
      },
      error: () => {
        this.uploadError = 'Text upload failed.';
        this.uploading = false;
      }
    });
  }

  refreshRecruiters(): void {
    this.recruiterService.getAll().subscribe(list => this.recruiters = list);
  }

  sendAllPending(): void {
    this.bulkSending = true;
    this.bulkResult = null;
    this.recruiterService.sendAllPending().subscribe({
      next: (res) => {
        this.bulkResult = res;
        this.bulkSending = false;
        this.refreshRecruiters();
        this.refreshQuota();
      },
      error: () => {
        this.bulkSending = false;
      }
    });
  }

  removeRecruiter(id: number): void {
    this.recruiterService.delete(id).subscribe(() => this.refreshRecruiters());
  }

  get pendingCount(): number {
    return this.recruiters.filter(r => r.status === 'PENDING').length;
  }

  badgeClass(status: string): string {
    if (status === 'SENT') return 'badge badge-sent';
    if (status === 'FOLLOWED_UP') return 'badge badge-sent';
    if (status === 'FAILED') return 'badge badge-failed';
    return 'badge badge-pending';
  }
}
