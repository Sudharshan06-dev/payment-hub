import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { RequestService } from '../../services/request.service';
import { Bill, BillsResponse, BillStatus } from '../../bill.model';
import { CommonModule } from '@angular/common';
import { PaymentComponent } from "../payment/payment.component";

@Component({
  selector: 'app-bills',
  templateUrl: './bills.component.html',
  styleUrls: ['./bills.component.css'],
  standalone: true,
  imports: [CommonModule, PaymentComponent]
})
export class BillsComponent implements OnInit, OnDestroy {
  bills: Bill[] = [];
  filteredBills: Bill[] = [];
  billStats: BillsResponse | null = null;

  isLoading = false;
  error: string | null = null;
  selectedBill: Bill | null = null;
  showPaymentModal = false;

  statusFilter: 'ALL' | BillStatus = 'ALL';
  sortBy: 'dueDate' | 'amount' | 'billerName' = 'dueDate';
  searchQuery = '';

  pageSize = 10;
  currentPage = 0;
  paginatedBills: Bill[] = [];

  private destroy$ = new Subject<void>();

  statusBadgeClass = {
    [BillStatus.DUE]: 'badge-warning',
    [BillStatus.OVERDUE]: 'badge-danger',
    [BillStatus.PENDING]: 'badge-info',
    [BillStatus.PAID]: 'badge-success'
  };

  statusBadgeIcon = {
    [BillStatus.DUE]: 'schedule',
    [BillStatus.OVERDUE]: 'error',
    [BillStatus.PENDING]: 'hourglass_empty',
    [BillStatus.PAID]: 'check_circle'
  };

  // Simulated bills data
  private simulatedBills =  [
    {
      id: 'BILL001',
      billerName: 'Electric Company',
      amount: 125.50,
      dueDate: new Date(2026, 1, 15),
      status: BillStatus.DUE,
      category: 'Utilities',
      recurring: true,
      billerLogo: 'https://via.placeholder.com/48?text=EC'
    },
    {
      id: 'BILL002',
      billerName: 'Internet Service Provider',
      amount: 79.99,
      dueDate: new Date(2026, 1, 10),
      status: BillStatus.OVERDUE,
      category: 'Utilities',
      recurring: true,
      billerLogo: 'https://via.placeholder.com/48?text=ISP'
    },
    {
      id: 'BILL003',
      billerName: 'Mortgage Payment',
      amount: 1500.00,
      dueDate: new Date(2026, 2, 1),
      status: BillStatus.PENDING,
      category: 'Housing',
      recurring: true,
      billerLogo: 'https://via.placeholder.com/48?text=MORT'
    },
    {
      id: 'BILL004',
      billerName: 'Netflix Subscription',
      amount: 15.99,
      dueDate: new Date(2026, 2, 5),
      status: BillStatus.PENDING,
      category: 'Subscription',
      recurring: true,
      billerLogo: 'https://via.placeholder.com/48?text=NETFLIX'
    },
    {
      id: 'BILL005',
      billerName: 'Water Department',
      amount: 45.75,
      dueDate: new Date(2026, 1, 20),
      status: BillStatus.DUE,
      category: 'Utilities',
      recurring: true,
      billerLogo: 'https://via.placeholder.com/48?text=WATER'
    },
    {
      id: 'BILL006',
      billerName: 'Gas Company',
      amount: 88.30,
      dueDate: new Date(2026, 1, 12),
      status: BillStatus.OVERDUE,
      category: 'Utilities',
      recurring: true,
      billerLogo: 'https://via.placeholder.com/48?text=GAS'
    },
    {
      id: 'BILL007',
      billerName: 'Car Insurance',
      amount: 125.00,
      dueDate: new Date(2026, 2, 10),
      status: BillStatus.PENDING,
      category: 'Insurance',
      recurring: true,
      billerLogo: 'https://via.placeholder.com/48?text=AUTO'
    },
    {
      id: 'BILL008',
      billerName: 'Health Insurance Premium',
      amount: 450.00,
      dueDate: new Date(2026, 1, 28),
      status: BillStatus.PENDING,
      category: 'Insurance',
      recurring: true,
      billerLogo: 'https://via.placeholder.com/48?text=HEALTH'
    },
    {
      id: 'BILL009',
      billerName: 'Credit Card Payment',
      amount: 250.00,
      dueDate: new Date(2026, 2, 5),
      status: BillStatus.PENDING,
      category: 'Credit',
      recurring: false,
      billerLogo: 'https://via.placeholder.com/48?text=CC'
    },
    {
      id: 'BILL010',
      billerName: 'Medical Bills - Hospital',
      amount: 325.50,
      dueDate: new Date(2026, 1, 5),
      status: BillStatus.PAID,
      category: 'Medical',
      recurring: false,
      billerLogo: 'https://via.placeholder.com/48?text=HOSPITAL'
    },
    {
      id: 'BILL011',
      billerName: 'Gym Membership',
      amount: 49.99,
      dueDate: new Date(2026, 2, 1),
      status: BillStatus.PENDING,
      category: 'Subscription',
      recurring: true,
      billerLogo: 'https://via.placeholder.com/48?text=GYM'
    },
    {
      id: 'BILL012',
      billerName: 'Property Tax',
      amount: 800.00,
      dueDate: new Date(2026, 3, 15),
      status: BillStatus.PENDING,
      category: 'Taxes',
      recurring: false,
      billerLogo: 'https://via.placeholder.com/48?text=TAX'
    }
  ];

  constructor(
    private requestService: RequestService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadBills();
  }

  loadBills(): void {
    this.isLoading = true;
    this.error = null;

    // Simulate API call with setTimeout
    setTimeout(() => {
      try {
        this.bills = JSON.parse(JSON.stringify(this.simulatedBills));
        
        // Calculate stats
        const total = this.bills.length;
        const pending = this.bills.filter(b => b.status === BillStatus.PENDING).length;
        const due = this.bills.filter(b => b.status === BillStatus.DUE).length;
        const overdue = this.bills.filter(b => b.status === BillStatus.OVERDUE).length;

        this.billStats = {
          bills: this.bills,
          total,
          pending,
          due,
          overdue
        };

        this.applyFiltersAndSort();
        this.updatePagination();
        this.isLoading = false;
      } catch (err) {
        console.error('Error loading bills:', err);
        this.error = 'Failed to load bills. Please try again.';
        this.isLoading = false;
      }
    }, 500);
  }

  applyFiltersAndSort(): void {
    let result = [...this.bills];

    if (this.statusFilter !== 'ALL') {
      result = result.filter(bill => bill.status === this.statusFilter);
    }

    if (this.searchQuery.trim()) {
      const query = this.searchQuery.toLowerCase();
      result = result.filter(bill =>
        bill.billerName.toLowerCase().includes(query) ||
        bill.id.toLowerCase().includes(query) ||
        bill.category?.toLowerCase().includes(query)
      );
    }

    result.sort((a : any, b : any) => {
      switch (this.sortBy) {
        case 'dueDate':
          return new Date(a.dueDate).getTime() - new Date(b.dueDate).getTime();
        case 'amount':
          return b.amount - a.amount;
        case 'billerName':
          return a.billerName.localeCompare(b.billerName);
        default:
          return 0;
      }
    });

    this.filteredBills = result;
  }

  updatePagination(): void {
    const start = this.currentPage * this.pageSize;
    const end = start + this.pageSize;
    this.paginatedBills = this.filteredBills.slice(start, end);
  }

  onStatusFilterChange(status: string): void {
    this.statusFilter = status as any;
    this.currentPage = 0;
    this.applyFiltersAndSort();
    this.updatePagination();
  }

  onSortChange(sortBy: any): void {
    this.sortBy = sortBy as any;
    this.applyFiltersAndSort();
    this.updatePagination();
  }

  onSearch(query: any): void {
    this.searchQuery = query;
    this.currentPage = 0;
    this.applyFiltersAndSort();
    this.updatePagination();
  }

  onPageChange(page: number): void {
    this.currentPage = page;
    this.updatePagination();
  }

  getDaysUntilDue(dueDate: Date): number {
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const due = new Date(dueDate);
    due.setHours(0, 0, 0, 0);

    const diff = due.getTime() - today.getTime();
    return Math.ceil(diff / (1000 * 60 * 60 * 24));
  }

  getDaysUntilDueText(dueDate: Date): string {
    const days = this.getDaysUntilDue(dueDate);

    if (days < 0) {
      return `${Math.abs(days)} day${Math.abs(days) > 1 ? 's' : ''} overdue`;
    } else if (days === 0) {
      return 'Due today';
    } else if (days === 1) {
      return 'Due tomorrow';
    } else {
      return `Due in ${days} day${days > 1 ? 's' : ''}`;
    }
  }

  formatCurrency(amount: any): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(amount);
  }

  openPaymentModal(bill: Bill): void {
    this.selectedBill = bill;
    this.showPaymentModal = true;
  }

  closePaymentModal(): void {
    this.showPaymentModal = false;
    this.selectedBill = null;
  }

  onPaymentSuccess(billId: string): void {
    const bill = this.bills.find(b => b.id === billId);
    if (bill) {
      bill.status = BillStatus.PAID;
      this.applyFiltersAndSort();
      this.updatePagination();
      
      // Recalculate stats
      const pending = this.bills.filter(b => b.status === BillStatus.PENDING).length;
      const due = this.bills.filter(b => b.status === BillStatus.DUE).length;
      const overdue = this.bills.filter(b => b.status === BillStatus.OVERDUE).length;
      
      if (this.billStats) {
        this.billStats.pending = pending;
        this.billStats.due = due;
        this.billStats.overdue = overdue;
      }
    }
    this.closePaymentModal();
  }

  refreshBills(): void {
    this.loadBills();
  }

  getTotalPages(): number {
    return Math.ceil(this.filteredBills.length / this.pageSize);
  }

  getPaginationArray(): number[] {
    const totalPages = this.getTotalPages();
    return Array.from({ length: totalPages }, (_, i) => i);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}