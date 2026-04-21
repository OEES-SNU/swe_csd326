import { Routes, Route, Navigate } from 'react-router-dom'
import { useAuth } from './context/AuthContext'

import HomePage from './pages/HomePage'
import Login from './pages/Login'

import AdminDashboard from './pages/admin/AdminDashboard'
import InstructorDashboard from './pages/instructor/InstructorDashboard'
import StudentDashboard from './pages/student/StudentDashboard'
import TakeExam from './pages/student/TakeExam'

function ProtectedRoute({ children, role }) {
    const { user, isAuthReady } = useAuth()

    if (!isAuthReady) return null

    if (!user) return <Navigate to="/login" replace />
    if (role && user.role !== role) return <Navigate to="/login" replace />

    return children
}

function RoleRedirect() {
    const { user, isAuthReady } = useAuth()

    if (!isAuthReady) return null

    if (!user) return <Navigate to="/login" replace />
    if (user.role === 'ADMIN') return <Navigate to="/admin" replace />
    if (user.role === 'INSTRUCTOR') return <Navigate to="/instructor" replace />
    if (user.role === 'STUDENT') return <Navigate to="/student" replace />

    return <Navigate to="/login" replace />
}

export default function App() {
    return (
        <Routes>
            {/* Public Routes */}
            <Route path="/" element={<HomePage />} />
            <Route path="/login" element={<Login />} />

            {/* Role Redirect Route */}
            <Route path="/dashboard" element={<RoleRedirect />} />

            {/* Admin */}
            <Route
                path="/admin/*"
                element={
                    <ProtectedRoute role="ADMIN">
                        <AdminDashboard />
                    </ProtectedRoute>
                }
            />

            {/* Instructor */}
            <Route
                path="/instructor/*"
                element={
                    <ProtectedRoute role="INSTRUCTOR">
                        <InstructorDashboard />
                    </ProtectedRoute>
                }
            />

            {/* Student */}
            <Route
                path="/student/*"
                element={
                    <ProtectedRoute role="STUDENT">
                        <StudentDashboard />
                    </ProtectedRoute>
                }
            />

            <Route
                path="/student/exam/:examId"
                element={
                    <ProtectedRoute role="STUDENT">
                        <TakeExam />
                    </ProtectedRoute>
                }
            />

            {/* Catch All */}
            <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
    )
}