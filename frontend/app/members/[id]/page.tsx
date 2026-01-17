'use client'

import { useEffect, useState } from 'react'
import { useParams, useRouter } from 'next/navigation'
import { memberService } from '@/lib/api/memberService'
import { transactionService } from '@/lib/api/transactionService'
import { Member, Transaction } from '@/lib/types'
import { ArrowLeft, Award, Calendar, CreditCard, Mail, Phone, User, TrendingUp } from 'lucide-react'
import Link from 'next/link'

export default function MemberDetailsPage() {
    const params = useParams()
    const router = useRouter()
    const id = Number(params.id)

    const [member, setMember] = useState<Member | null>(null)
    const [transactions, setTransactions] = useState<Transaction[]>([])
    const [loading, setLoading] = useState(true)
    const [activeTab, setActiveTab] = useState<'OVERVIEW' | 'HISTORY'>('OVERVIEW')

    useEffect(() => {
        if (id) {
            loadData()
        }
    }, [id])

    const loadData = async () => {
        try {
            setLoading(true)
            const [memberData, txData] = await Promise.all([
                memberService.getById(id),
                transactionService.getByMember(id)
            ])
            setMember(memberData)
            setTransactions(txData || [])
        } catch (err) {
            console.error('Failed to load member data:', err)
        } finally {
            setLoading(false)
        }
    }

    const getTierColor = (tier: string) => {
        const colors = {
            PLATINUM: 'bg-purple-100 text-purple-800 border-purple-200',
            GOLD: 'bg-yellow-100 text-yellow-800 border-yellow-200',
            SILVER: 'bg-gray-100 text-gray-800 border-gray-200',
            BRONZE: 'bg-orange-100 text-orange-800 border-orange-200',
            DIAMOND: 'bg-blue-100 text-blue-800 border-blue-200',
        }
        return colors[tier as keyof typeof colors] || 'bg-gray-100 text-gray-800 border-gray-200'
    }

    const calculateTierProgress = (points: number) => {
        const tiers = [
            { name: 'BRONZE', threshold: 0 },
            { name: 'SILVER', threshold: 1000 },
            { name: 'GOLD', threshold: 5000 },
            { name: 'PLATINUM', threshold: 15000 },
            { name: 'DIAMOND', threshold: 50000 },
        ]

        let currentTierIndex = 0
        for (let i = 0; i < tiers.length; i++) {
            if (points >= tiers[i].threshold) {
                currentTierIndex = i
            } else {
                break
            }
        }

        const currentTier = tiers[currentTierIndex]
        const nextTier = tiers[currentTierIndex + 1]

        if (!nextTier) {
            return {
                currentTier: currentTier.name,
                nextTier: null,
                progress: 100,
                pointsNeeded: 0
            }
        }

        const pointsInTier = points - currentTier.threshold
        const tierSpan = nextTier.threshold - currentTier.threshold
        const progress = Math.min(100, Math.max(0, (pointsInTier / tierSpan) * 100))

        return {
            currentTier: currentTier.name,
            nextTier: nextTier.name,
            progress,
            pointsNeeded: nextTier.threshold - points
        }
    }

    if (loading) {
        return (
            <div className="min-h-screen flex justify-center items-center">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
            </div>
        )
    }

    if (!member) {
        return (
            <div className="text-center py-12">
                <h2 className="text-2xl font-bold text-gray-900">Member Not Found</h2>
                <Link href="/members" className="text-blue-600 hover:text-blue-800 mt-4 inline-block">
                    &larr; Back to Members
                </Link>
            </div>
        )
    }

    const tierProgress = calculateTierProgress(member.lifetimePoints || 0)

    return (
        <div className="space-y-6">
            {/* Header */}
            <div>
                <Link href="/members" className="flex items-center text-sm text-gray-500 hover:text-gray-700 mb-4 transition-colors">
                    <ArrowLeft size={16} className="mr-1" />
                    Back to Members
                </Link>
                <div className="flex justify-between items-start">
                    <div>
                        <h1 className="text-3xl font-bold text-gray-900 flex items-center gap-3">
                            {member.name}
                            <span className={`px-3 py-1 rounded-full text-sm font-bold border ${getTierColor(member.tier)}`}>
                                {member.tier}
                            </span>
                        </h1>
                        <div className="flex gap-6 mt-3 text-sm text-gray-600">
                            <div className="flex items-center gap-1">
                                <Mail size={16} />
                                {member.email}
                            </div>
                            <div className="flex items-center gap-1">
                                <Phone size={16} />
                                {member.phone}
                            </div>
                            <div className="flex items-center gap-1">
                                <Calendar size={16} />
                                Joined {member.createdAt ? new Date(member.createdAt).toLocaleDateString() : 'N/A'}
                            </div>
                        </div>
                    </div>
                    <button className="bg-white border border-gray-300 text-gray-700 hover:bg-gray-50 px-4 py-2 rounded-lg text-sm font-medium transition-colors">
                        Edit Profile
                    </button>
                </div>
            </div>

            {/* Stats Overview */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
                    <div className="flex justify-between items-start">
                        <div>
                            <p className="text-sm font-medium text-gray-500 uppercase tracking-wider">Current Points</p>
                            <h3 className="text-3xl font-bold text-gray-900 mt-1">{member.totalPoints.toLocaleString()}</h3>
                        </div>
                        <div className="p-3 bg-blue-50 rounded-lg">
                            <Award className="w-6 h-6 text-blue-600" />
                        </div>
                    </div>
                </div>
                <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
                    <div className="flex justify-between items-start">
                        <div>
                            <p className="text-sm font-medium text-gray-500 uppercase tracking-wider">Lifetime Points</p>
                            <h3 className="text-3xl font-bold text-gray-900 mt-1">{member.lifetimePoints.toLocaleString()}</h3>
                        </div>
                        <div className="p-3 bg-indigo-50 rounded-lg">
                            <TrendingUp className="w-6 h-6 text-indigo-600" />
                        </div>
                    </div>
                </div>
                <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
                    <div className="flex justify-between items-start">
                        <div>
                            <p className="text-sm font-medium text-gray-500 uppercase tracking-wider">Total Transactions</p>
                            <h3 className="text-3xl font-bold text-gray-900 mt-1">{transactions.length}</h3>
                        </div>
                        <div className="p-3 bg-green-50 rounded-lg">
                            <CreditCard className="w-6 h-6 text-green-600" />
                        </div>
                    </div>
                </div>
            </div>

            {/* Tier Progress */}
            <div className="bg-gradient-to-r from-gray-900 to-gray-800 rounded-xl shadow-lg p-6 text-white relative overflow-hidden">
                <div className="absolute top-0 right-0 p-8 opacity-10">
                    <Award size={120} />
                </div>
                <div className="relative z-10">
                    <h3 className="text-lg font-bold mb-4 flex items-center gap-2">
                        <Award size={20} className="text-yellow-400" />
                        Tier Progress
                    </h3>

                    {tierProgress.nextTier ? (
                        <div className="space-y-4">
                            <div className="flex justify-between text-sm font-medium">
                                <span className="text-gray-300">Current: <span className="text-white">{tierProgress.currentTier}</span></span>
                                <span className="text-yellow-400">Next: {tierProgress.nextTier}</span>
                            </div>

                            <div className="w-full bg-gray-700/50 rounded-full h-3 backdrop-blur-sm">
                                <div
                                    className="bg-gradient-to-r from-blue-500 to-cyan-400 h-3 rounded-full transition-all duration-1000 ease-out"
                                    style={{ width: `${tierProgress.progress}%` }}
                                ></div>
                            </div>

                            <p className="text-sm text-gray-300">
                                You need <span className="font-bold text-white">{tierProgress.pointsNeeded.toLocaleString()}</span> more points to reach {tierProgress.nextTier} status.
                            </p>
                        </div>
                    ) : (
                        <div className="text-center py-4">
                            <Award className="w-12 h-12 text-yellow-400 mx-auto mb-3" />
                            <h4 className="text-xl font-bold text-white">Maximum Tier Reached!</h4>
                            <p className="text-gray-400 text-sm">You have achieved our highest status level.</p>
                        </div>
                    )}
                </div>
            </div>

            {/* Transactions History */}
            <div className="bg-white rounded-xl shadow-md border border-gray-100 overflow-hidden">
                <div className="px-6 py-4 border-b border-gray-100">
                    <h3 className="font-bold text-gray-900">Transaction History</h3>
                </div>

                {transactions.length === 0 ? (
                    <div className="text-center py-12 text-gray-500">
                        No transactions found for this member.
                    </div>
                ) : (
                    <table className="min-w-full divide-y divide-gray-200">
                        <thead className="bg-gray-50">
                            <tr>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Date</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Transaction ID</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Payment Method</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Amount</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Points Earned</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                            </tr>
                        </thead>
                        <tbody className="bg-white divide-y divide-gray-200">
                            {transactions.map((tx) => (
                                <tr key={tx.id} className="hover:bg-gray-50">
                                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                        {tx.transactionDate ? new Date(tx.transactionDate).toLocaleDateString() + ' ' + new Date(tx.transactionDate).toLocaleTimeString() : 'N/A'}
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap text-sm font-mono text-gray-500">
                                        #{tx.id}
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap">
                                        <div className="flex flex-col">
                                            <span className="text-sm font-medium text-gray-900">{tx.paymentMethod}</span>
                                        </div>
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap text-sm font-bold text-gray-900">
                                        ${Number(tx.amount).toFixed(2)}
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap">
                                        {tx.pointsEarned ? (
                                            <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                                                +{tx.pointsEarned} pts
                                            </span>
                                        ) : (
                                            <span className="text-gray-400 text-xs">-</span>
                                        )}
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap">
                                        <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${tx.status === 'COMPLETED' ? 'bg-green-100 text-green-800' :
                                                tx.status === 'PENDING' ? 'bg-yellow-100 text-yellow-800' : 'bg-red-100 text-red-800'
                                            }`}>
                                            {tx.status}
                                        </span>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                )}
            </div>
        </div>
    )
}
