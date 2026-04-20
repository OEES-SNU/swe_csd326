import React from 'react'
import { Link } from 'react-router-dom'
import { motion } from 'framer-motion'
import { ArrowRight, ShieldCheck, Users, ClipboardList, BarChart3, GraduationCap, BookOpen } from 'lucide-react'

export default function HomePage() {
    const features = [
        { icon: ShieldCheck, title: 'Secure Login', desc: 'Role-based authentication for Admin, Instructor and Student.' },
        { icon: BookOpen, title: 'Course Control', desc: 'Manage courses, instructors and enrollments easily.' },
        { icon: ClipboardList, title: 'Online Exams', desc: 'Create, schedule and conduct exams digitally.' },
        { icon: BarChart3, title: 'Results & Reports', desc: 'Generate results and view performance insights.' },
    ]

    return (
        <div className="min-h-screen bg-gradient-to-br from-slate-950 via-slate-900 to-slate-800 text-white overflow-hidden relative">
            <motion.div animate={{ y:[0,-20,0], x:[0,15,0] }} transition={{ repeat: Infinity, duration: 8 }} className="absolute top-24 left-10 w-40 h-40 bg-cyan-400/20 blur-3xl rounded-full" />
            <motion.div animate={{ y:[0,25,0], x:[0,-10,0] }} transition={{ repeat: Infinity, duration: 10 }} className="absolute bottom-20 right-10 w-52 h-52 bg-fuchsia-400/20 blur-3xl rounded-full" />

            <div className="max-w-7xl mx-auto px-6 relative z-10">
                <header className="py-6 flex justify-between items-center">
                    <div className="flex items-center gap-3">
                        <div className="p-3 bg-white/10 rounded-2xl"><GraduationCap /></div>
                        <div>
                            <h1 className="font-bold text-xl">OEES</h1>
                            <p className="text-slate-300 text-sm">Online Examination & Evaluation System</p>
                        </div>
                    </div>
                    <Link to="/login" className="px-5 py-2 rounded-xl bg-white text-slate-900 font-semibold hover:bg-slate-200 transition">Login</Link>
                </header>

                <section className="grid lg:grid-cols-2 gap-12 py-16 items-center">
                    <motion.div initial={{opacity:0,y:20}} animate={{opacity:1,y:0}} transition={{duration:0.7}}>
                        <p className="uppercase tracking-[0.3em] text-cyan-300 text-sm">Smart Exam Platform</p>
                        <h2 className="text-5xl lg:text-6xl font-bold mt-5 leading-tight">Manage Exams, Evaluation & Results Seamlessly</h2>
                        <p className="mt-6 text-slate-300 text-lg leading-8">A centralized portal for admins, instructors and students to run the complete examination lifecycle.</p>
                        <div className="mt-8 flex gap-4 flex-wrap">
                            <Link to="/login" className="px-6 py-3 rounded-xl bg-cyan-400 text-slate-950 font-semibold flex items-center gap-2 hover:bg-cyan-300 transition">Login Now <ArrowRight size={18} /></Link>
                            <a href="#features" className="px-6 py-3 rounded-xl border border-white/20 hover:bg-white/10 transition">Explore Features</a>
                        </div>
                    </motion.div>

                    <motion.div initial={{opacity:0,scale:0.95}} animate={{opacity:1,scale:1}} transition={{duration:0.8}} className="bg-white/10 backdrop-blur-xl border border-white/10 rounded-3xl p-8 shadow-2xl">
                        <h3 className="text-2xl font-bold">System Workflow</h3>
                        <div className="space-y-4 mt-6">
                            {['Admin creates users & courses','Instructor creates exams','Students attempt scheduled exams','Results and analytics generated'].map((item,i)=>(<div key={i} className="flex gap-4 items-start bg-white/5 rounded-2xl p-4"><div className="w-8 h-8 rounded-full bg-cyan-400 text-slate-950 flex items-center justify-center font-bold">{i+1}</div><p>{item}</p></div>))}
                        </div>
                    </motion.div>
                </section>

                <section id="features" className="py-10">
                    <h3 className="text-3xl font-bold text-center">Core Features</h3>
                    <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-6 mt-10">
                        {features.map((f,i)=>{ const Icon=f.icon; return <div key={i} className="bg-white/10 border border-white/10 rounded-3xl p-6 hover:-translate-y-2 transition shadow-xl"><Icon className="text-cyan-300" /><h4 className="mt-4 text-xl font-semibold">{f.title}</h4><p className="mt-3 text-slate-300 text-sm leading-6">{f.desc}</p></div>})}
                    </div>
                </section>

                <section className="py-16 text-center">
                    <Users className="mx-auto text-cyan-300" size={34} />
                    <h3 className="text-3xl font-bold mt-4">Built For Every Role</h3>
                    <p className="text-slate-300 mt-3">Admin • Instructor • Student</p>
                    <Link to="/login" className="inline-flex mt-8 px-7 py-3 rounded-xl bg-white text-slate-900 font-semibold items-center gap-2 hover:bg-slate-200 transition">Get Started <ArrowRight size={18} /></Link>
                </section>
            </div>
        </div>
    )
}
