import { Routes, Route, Navigate } from 'react-router-dom'
import Layout from '../../components/Layout'
import Questions from './Questions'
import Exams from './Exams'
import Evaluation from './Evaluation'

export default function InstructorDashboard() {
  return (
    <Layout>
      <Routes>
        <Route path="questions" element={<Questions />} />
        <Route path="exams" element={<Exams />} />
        <Route path="evaluation" element={<Evaluation />} />
        <Route path="*" element={<Navigate to="exams" replace />} />
      </Routes>
    </Layout>
  )
}
