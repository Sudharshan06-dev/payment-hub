import { Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { AuthGuard } from '../guards/AuthGuard';
import { BillsComponent } from './bills/bills.component';

export const routes: Routes = [
    {path: '', component: LoginComponent},
    {path: 'login', component: LoginComponent},
    {
    path: 'dashboard', // Matches the parent 'dashboard' path
    component: DashboardComponent,
    canActivate: [AuthGuard],
    children: [
      {
        path: '',
        component: BillsComponent
      },
      {
        path: 'payments',
        component: BillsComponent 
      }
    ]
  }
];
