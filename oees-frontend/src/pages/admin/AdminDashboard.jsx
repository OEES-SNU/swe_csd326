import { Routes, Route, Navigate } from 'react-router-dom'
import Layout from '../../components/Layout'
import Courses from './Courses'
import Students from './Students'
import Enrollments from './Enrollments'

export default function AdminDashboard() {
  return (
    <Layout>
      <Routes>
        <Route path="courses" element={<Courses />} />
        <Route path="students" element={<Students />} />
        <Route path="enrollments" element={<Enrollments />} />
        <Route path="*" element={<Navigate to="courses" replace />} />
      </Routes>
    </Layout>
  )
}
