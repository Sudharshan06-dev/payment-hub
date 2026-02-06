
// app.routes.ts - CORRECTED
import { Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { AuthGuard } from '../guards/AuthGuard';
import { BillsComponent } from './bills/bills.component';
import { SettingsComponent } from './settings/settings.component';

export const routes: Routes = [
    // Public routes
    {path: '', component: LoginComponent},
    {path: 'login', component: LoginComponent},
    
    // Protected parent route with children
    {
        path: 'dashboard',
        component: DashboardComponent,
        canActivate: [AuthGuard],
        children: [
            // Default child route - shows when you go to /dashboard
            {
                path: '',
                component: BillsComponent
            },
            {
                path: 'settings',
                component: SettingsComponent
            }
            // Add more child routes here as needed
            // {
            //     path: 'payments',
            //     component: PaymentsComponent
            // },
            // {
            //     path: 'profile',
            //     component: ProfileComponent
            // }
        ]
    },
    
    // Catch-all redirect (should be last)
    //{path: '**', redirectTo: '/login'}
];