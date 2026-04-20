import { useEffect, useState } from 'react'
import api from '../../api/axios'

export default function Analytics() {
    const [stats, setStats] = useState(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')

    // change exam id later dynamically
    const examId = 1

    useEffect(() => {
        loadAnalytics()
    }, [])

    const loadAnalytics = async () => {
        try {
            setLoading(true)
            const { data } = await api.get(
                `/results/analytics/exam/${examId}`
            )
            setStats(data)
        } catch (err) {
            setError('Failed to load analytics')
        } finally {
            setLoading(false)
        }
    }

    if (loading) {
        return <div className="p-6">Loading analytics...</div>
    }

    if (error) {
        return <div className="p-6 text-red-600">{error}</div>
    }

    return (
        <>
            <div className="page-header">
                <div>
                    <h1 className="page-title">Analytics</h1>
                    <p className="page-subtitle">
                        Student performance insights
                    </p>
                </div>
            </div>

            {/* Cards */}
            <div className="grid md:grid-cols-4 gap-4 mb-6">
                <div className="card p-5">
                    <p className="text-sm text-gray-500">Attempts</p>
                    <h2 className="text-2xl font-bold">
                        {stats.totalAttempts ?? 0}
                    </h2>
                </div>

                <div className="card p-5">
                    <p className="text-sm text-gray-500">Average Score</p>
                    <h2 className="text-2xl font-bold">
                        {stats.averageScore ?? 0}
                    </h2>
                </div>

                <div className="card p-5">
                    <p className="text-sm text-gray-500">Highest Score</p>
                    <h2 className="text-2xl font-bold">
                        {stats.highestScore ?? 0}
                    </h2>
                </div>

                <div className="card p-5">
                    <p className="text-sm text-gray-500">Pass %</p>
                    <h2 className="text-2xl font-bold">
                        {stats.passPercentage ?? 0}%
                    </h2>
                </div>
            </div>

            {/* Top Students */}
            <div className="card p-6 mb-6">
                <h2 className="text-lg font-semibold mb-4">
                    Top Students
                </h2>

                <table className="w-full text-sm">
                    <thead>
                    <tr className="text-left border-b">
                        <th className="py-2">Rank</th>
                        <th>Name</th>
                        <th>Score</th>
                    </tr>
                    </thead>

                    <tbody>
                    {(stats.topStudents || []).map((s, i) => (
                        <tr key={i} className="border-b">
                            <td className="py-2">{i + 1}</td>
                            <td>{s.name}</td>
                            <td>{s.score}</td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>

            {/* Question Difficulty */}
            <div className="card p-6">
                <h2 className="text-lg font-semibold mb-4">
                    Difficult Questions
                </h2>

                <div className="space-y-3">
                    {(stats.questionDifficulty || []).map((q, i) => (
                        <div
                            key={i}
                            className="flex justify-between border-b pb-2"
                        >
                            <span>{q.question}</span>
                            <span>{q.incorrectPercentage}% wrong</span>
                        </div>
                    ))}
                </div>
            </div>
        </>
    )
}