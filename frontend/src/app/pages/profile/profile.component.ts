import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ProfileService } from '../../services/profile.service';
import { Profile } from '../../models/profile.model';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css'
})
export class ProfileComponent implements OnInit {

  profile: Profile | null = null;

  fullName = '';
  senderEmail = '';
  defaultSubject = '';
  defaultDescription = '';
  followUpSubject = '';
  followUpDescription = '';

  selectedResume: File | null = null;
  saving = false;
  uploadingResume = false;
  message = '';

  constructor(private profileService: ProfileService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.profileService.getProfile().subscribe(p => {
      this.profile = p;
      this.fullName = p.fullName || '';
      this.senderEmail = p.senderEmail || '';
      this.defaultSubject = p.defaultSubject || '';
      this.defaultDescription = p.defaultDescription || '';
      this.followUpSubject = p.followUpSubject || '';
      this.followUpDescription = p.followUpDescription || '';
    });
  }

  saveDetails(): void {
    this.saving = true;
    this.profileService.updateDetails({
      fullName: this.fullName,
      defaultSubject: this.defaultSubject,
      defaultDescription: this.defaultDescription,
      senderEmail: this.senderEmail,
      followUpSubject: this.followUpSubject,
      followUpDescription: this.followUpDescription
    }).subscribe({
      next: (p) => {
        this.profile = p;
        this.saving = false;
        this.message = 'Profile saved successfully.';
      },
      error: () => {
        this.saving = false;
        this.message = 'Failed to save profile.';
      }
    });
  }

  onResumeSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedResume = input.files && input.files.length ? input.files[0] : null;
  }

  uploadResume(): void {
    if (!this.selectedResume) return;
    this.uploadingResume = true;
    this.profileService.uploadResume(this.selectedResume).subscribe({
      next: (p) => {
        this.profile = p;
        this.uploadingResume = false;
        this.selectedResume = null;
        this.message = 'Resume uploaded successfully.';
      },
      error: () => {
        this.uploadingResume = false;
        this.message = 'Resume upload failed.';
      }
    });
  }
}
