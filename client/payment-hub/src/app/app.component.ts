import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { LocalStorageHelper } from '../services/local-storage.service';
import { CommonModule } from '@angular/common';
import { NgxSpinnerComponent } from 'ngx-spinner';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, CommonModule, NgxSpinnerComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  public readonly title = 'payment-hub';

  public readonly spinnerTemplate = `
  <div class="spinner-wrapper">
  <svg class="spinner-svg" viewBox="0 0 120 120" xmlns="http://www.w3.org/2000/svg">
    <defs>
      <linearGradient id="spinnerGradient" x1="0%" y1="0%" x2="100%" y2="100%">
        <stop offset="0%" style="stop-color:#1e40af;stop-opacity:1" />
        <stop offset="100%" style="stop-color:#0891b2;stop-opacity:1" />
      </linearGradient>
      
      <filter id="spinnerGlow">
        <feGaussianBlur stdDeviation="1.5" result="coloredBlur"/>
        <feMerge>
          <feMergeNode in="coloredBlur"/>
          <feMergeNode in="SourceGraphic"/>
        </feMerge>
      </filter>
    </defs>

    <!-- Background circle -->
    <circle cx="60" cy="60" r="50" fill="none" stroke="#e2e8f0" stroke-width="1.5" opacity="0.2"/>

    <!-- Main rotating ring -->
    <circle
      class="spinner-ring"
      cx="60"
      cy="60"
      r="45"
      fill="none"
      stroke="url(#spinnerGradient)"
      stroke-width="3.5"
      stroke-linecap="round"
      stroke-dasharray="141 282"
      filter="url(#spinnerGlow)"
    />

    <!-- Secondary ring (opposite direction) -->
    <circle
      class="spinner-ring-secondary"
      cx="60"
      cy="60"
      r="32"
      fill="none"
      stroke="url(#spinnerGradient)"
      stroke-width="2.5"
      stroke-linecap="round"
      stroke-dasharray="100 200"
      opacity="0.5"
      filter="url(#spinnerGlow)"
    />

    <!-- Center dot pulse -->
    <circle class="spinner-center" cx="60" cy="60" r="3" fill="#1e40af" opacity="0.6"/>
  </svg>
</div>

<style>
  .spinner-wrapper {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 1rem;
    padding: 2rem;
  }

  .spinner-svg {
    width: 120px;
    height: 120px;
    filter: drop-shadow(0 8px 20px rgba(30, 64, 175, 0.15));
  }

  .spinner-ring {
    animation: spin-cw 3s linear infinite;
    transform-origin: 60px 60px;
  }

  .spinner-ring-secondary {
    animation: spin-ccw 4s linear infinite;
    transform-origin: 60px 60px;
  }

  .spinner-center {
    animation: pulse-center 2s ease-in-out infinite;
  }

  @keyframes spin-cw {
    from {
      transform: rotate(0deg);
      stroke-dashoffset: 0;
    }
    to {
      transform: rotate(360deg);
      stroke-dashoffset: -282;
    }
  }

  @keyframes spin-ccw {
    from {
      transform: rotate(0deg);
      stroke-dashoffset: 0;
    }
    to {
      transform: rotate(-360deg);
      stroke-dashoffset: -200;
    }
  }

  @keyframes pulse-center {
    0%, 100% {
      r: 3;
      opacity: 0.4;
    }
    50% {
      r: 5;
      opacity: 0.8;
    }
  }

  .spinner-text {
    font-size: 0.95rem;
    font-weight: 600;
    color: #0f172a;
    margin: 0;
    letter-spacing: 0.5px;
  }

  @media (max-width: 768px) {
    .spinner-svg {
      width: 100px;
      height: 100px;
    }
    
    .spinner-text {
      font-size: 0.9rem;
    }
  }
</style>
`;

  constructor(private localStorage: LocalStorageHelper) {
  }

  get userAuthenticated() {
    return !!this.localStorage.getItem('access_token')
  }

}