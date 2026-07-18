import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { EmailService } from '../../services/email.service';
import { EmailLog } from '../../models/email-log.model';

@Component({
  selector: 'app-logs',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './logs.component.html',
  styleUrl: './logs.component.css'
})
export class LogsComponent implements OnInit {
  logs: EmailLog[] = [];

  constructor(private emailService: EmailService) {}

  ngOnInit(): void {
    this.emailService.getLogs().subscribe(logs => {
      this.logs = logs.sort((a, b) => b.id - a.id);
    });
  }
}
