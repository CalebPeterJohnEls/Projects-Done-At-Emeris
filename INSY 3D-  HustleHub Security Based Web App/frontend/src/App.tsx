import React from "react";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import { NavBar } from "./components/NavBar.tsx";
import { ProtectedRoute } from "./components/ProtectedRoute.tsx";
import { AuthProvider } from "./context/AuthProvider.tsx";
import { GigsPage } from "./pages/GigsPages.tsx";
import { GigDetailPage } from "./pages/gigDetailPage.tsx";
import { LoginPage } from "./pages/LoginPage.tsx";
import { RegisterPage } from "./pages/RegisterPage.tsx";
import { MyBookingsPage } from "./pages/MyBookingsPage.tsx";
import { TransactionsPage } from "./pages/TransactionsPage.tsx";
import { DashboardPage } from "./pages/DashboardPage.tsx";
import { AdminPage } from "./pages/AdminPage.tsx";
import { SecurityPage } from "./pages/SecurityPage.tsx";
import { NotFoundPage } from "./pages/NotFoundPage.tsx";

// the route table, separate from the providers so tests can mount it in a MemoryRouter
export const AppRoutes: React.FC = () => (
  <>
    <NavBar />
    <main className='app-container'>
      <Routes>
        <Route path='/' element={<GigsPage />} />
        <Route path='/gigs/:id' element={<GigDetailPage />} />
        <Route path='/login' element={<LoginPage />} />
        <Route path='/register' element={<RegisterPage />} />

        <Route element={<ProtectedRoute roles={["client"]} />}>
          <Route path='/bookings' element={<MyBookingsPage />} />
        </Route>
        <Route element={<ProtectedRoute roles={["client", "freelancer"]} />}>
          <Route path='/transactions' element={<TransactionsPage />} />
        </Route>
        <Route element={<ProtectedRoute roles={["freelancer"]} />}>
          <Route path='/dashboard' element={<DashboardPage />} />
        </Route>
        <Route element={<ProtectedRoute roles={["admin"]} />}>
          <Route path='/admin' element={<AdminPage />} />
        </Route>
        <Route element={<ProtectedRoute roles={["client", "freelancer", "admin"]} />}>
          <Route path='/security' element={<SecurityPage />} />
        </Route>

        <Route path='*' element={<NotFoundPage />} />
      </Routes>
    </main>
  </>
);

export const App: React.FC = () => (
  <BrowserRouter>
    <AuthProvider>
      <AppRoutes />
    </AuthProvider>
  </BrowserRouter>
);

export default App;