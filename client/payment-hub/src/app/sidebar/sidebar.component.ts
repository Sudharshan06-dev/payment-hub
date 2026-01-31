import { Component, OnInit, Input } from '@angular/core';
import { Router } from '@angular/router';
import { BillsResponse } from '../../bill.model';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  standalone: true,
  styleUrls: ['./sidebar.component.css'],
  imports: [CommonModule]
})
export class SidebarComponent implements OnInit {
  @Input() billStats: BillsResponse | null = null;

  userInfo = {
    name: 'John Doe',
    email: 'john.doe@wellsfargo.com',
    avatar: 'https://via.placeholder.com/50?text=JD'
  };

  menuItems = [
    { label: 'Dashboard', icon: 'dashboard', route: '/dashboard' },
    { label: 'Bills', icon: 'receipt_long', route: '/dashboard' },
    { label: 'Payments', icon: 'payment', route: '/payments' },
    { label: 'Settings', icon: 'settings', route: '/settings' },
    { label: 'Help', icon: 'help', route: '/help' }
  ];

  constructor(private router: Router) {}

  ngOnInit(): void {}

  navigate(route: string): void {
    this.router.navigate([route]);
  }

  logout(): void {
    // Implement logout logic
    this.router.navigate(['/login']);
  }
}