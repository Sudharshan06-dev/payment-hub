import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { BillsResponse, Bill } from '../../bill.model';
import { CommonModule } from '@angular/common';
import { BillDataService } from '../../services/bill-data.service';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { LocalStorageHelper } from '../../services/local-storage.service';

interface UserInfo {
  user_id: number,
  name: string,
  email: string,
  username: string,
  avatar: string
}

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  standalone: true,
  styleUrls: ['./sidebar.component.css'],
  imports: [CommonModule]
})

export class SidebarComponent implements OnInit, OnDestroy {
  billStats: BillsResponse | null = null;
  bills: Bill[] = [];
  selectedBill: Bill | null = null;
  totalAmountOwed: number = 0;
  isLoading: boolean = false;
  userInfo : UserInfo | null = null;

  private destroy$ = new Subject<void>();

  menuItems = [
    { label: 'Dashboard', icon: 'dashboard', route: '/dashboard' },
    { label: 'Bills', icon: 'receipt_long', route: '/dashboard' },
    { label: 'Payments', icon: 'payment', route: '/payments' },
    { label: 'Settings', icon: 'settings', route: '/settings' },
    { label: 'Help', icon: 'help', route: '/help' }
  ];

  constructor(private router: Router, private billDataService: BillDataService, private localStorage: LocalStorageHelper) {
    const userDetails = this.localStorage.getItem('user_details');
    this.userInfo = {
      user_id: userDetails?.userId,
      name: userDetails?.fullName,
      username: userDetails?.username,
      email: userDetails?.email,
      avatar: userDetails?.fullName[0]
    }
  }

  ngOnInit(): void {
    // Subscribe to bill statistics
    this.billDataService.billStats$
      .pipe(takeUntil(this.destroy$))
      .subscribe(stats => {
        this.billStats = stats;
      });

    // Subscribe to bills
    this.billDataService.bills$
      .pipe(takeUntil(this.destroy$))
      .subscribe(bills => {
        this.bills = bills;
      });

    // Subscribe to selected bill
    this.billDataService.selectedBill$
      .pipe(takeUntil(this.destroy$))
      .subscribe(bill => {
        this.selectedBill = bill;
      });

    // Subscribe to total amount owed
    this.billDataService.totalAmountOwed$
      .pipe(takeUntil(this.destroy$))
      .subscribe(amount => {
        this.totalAmountOwed = amount;
      });

    // Subscribe to loading state
    this.billDataService.loading$
      .pipe(takeUntil(this.destroy$))
      .subscribe(loading => {
        this.isLoading = loading;
      });
  }

  navigate(route: string): void {
    this.router.navigate([route]);
  }

  logout(): void {
    // Implement logout logic
    this.router.navigate(['/login']);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}