import { Routes, Route, Navigate } from 'react-router-dom'
import Layout from '../../components/Layout'
import AvailableExams from './AvailableExams'
import Results from './Results'
import MyCourses from './MyCourses'

export default function StudentDashboard() {
  return (
    <Layout>
      <Routes>
        <Route path="courses" element={<MyCourses />} />
        <Route path="exams" element={<AvailableExams />} />
        <Route path="results" element={<Results />} />
        <Route path="*" element={<Navigate to="courses" replace />} />
      </Routes>
    </Layout>
  )
}
