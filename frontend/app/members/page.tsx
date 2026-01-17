'use client'

import { useEffect, useState } from 'react'
import { memberService } from '@/lib/api/memberService'
import { Member, PagedResponse } from '@/lib/types'
import Link from 'next/link'

export default function MembersPage() {
    const [memberPage, setMemberPage] = useState<PagedResponse<Member> | null>(null)
    const [loading, setLoading] = useState(true)
    const [currentPage, setCurrentPage] = useState(0)
    const [searchTerm, setSearchTerm] = useState('')

    useEffect(() => {
        loadMembers()
    }, [currentPage]) // Reload when page changes. Search will be handled by form submit or separate effect with debounce if needed.

    const loadMembers = async () => {
        try {
            setLoading(true)
            // Passing searchTerm if it's not empty, otherwise undefined
            const data = await memberService.getAll(currentPage, 20, searchTerm || undefined)
            setMemberPage(data)
        } catch (err) {
            console.error('Failed to load members:', err)
        } finally {
            setLoading(false)
        }
    }

    const handleSearch = (e: React.FormEvent) => {
        e.preventDefault()
        setCurrentPage(0) // Reset to first page on search
        loadMembers()
    }

    const getTierColor = (tier: string) => {
        const colors = {
            PLATINUM: 'bg-purple-100 text-purple-800',
            GOLD: 'bg-yellow-100 text-yellow-800',
            SILVER: 'bg-gray-100 text-gray-800',
            BRONZE: 'bg-orange-100 text-orange-800',
        }
        return colors[tier as keyof typeof colors] || 'bg-gray-100 text-gray-800'
    }

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <div>
                    <h1 className="text-3xl font-bold text-gray-900">Members</h1>
                    <p className="mt-2 text-gray-600">Manage customer members and their loyalty status</p>
                </div>
                <Link
                    href="/members/new"
                    className="bg-blue-600 hover:bg-blue-700 text-white px-6 py-3 rounded-lg font-medium transition-colors shadow-md hover:shadow-lg"
                >
                    + Add Member
                </Link>
            </div>

            {/* Search Bar */}
            <form onSubmit={handleSearch} className="flex gap-4">
                <input
                    type="text"
                    placeholder="Search by name, email or phone..."
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
                <button
                    type="submit"
                    className="bg-gray-800 hover:bg-gray-900 text-white px-6 py-2 rounded-lg font-medium transition-colors"
                >
                    Search
                </button>
            </form>

            {loading ? (
                <div className="text-center py-12">
                    <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
                </div>
            ) : (
                <>
                    <div className="bg-white rounded-xl shadow-md overflow-hidden border border-gray-100">
                        <table className="min-w-full divide-y divide-gray-200">
                            <thead className="bg-gray-50">
                                <tr>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        Member
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        Email
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        Tier
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        Points
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        Actions
                                    </th>
                                </tr>
                            </thead>
                            <tbody className="bg-white divide-y divide-gray-200">
                                {memberPage?.content.map((member) => (
                                    <tr key={member.id} className="hover:bg-gray-50 transition-colors">
                                        <td className="px-6 py-4 whitespace-nowrap">
                                            <div className="text-sm font-medium text-gray-900">{member.name}</div>
                                            <div className="text-sm text-gray-500">{member.phone}</div>
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                            {member.email}
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap">
                                            <span className={`px-3 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${getTierColor(member.tier)}`}>
                                                {member.tier}
                                            </span>
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 font-medium">
                                            {member.totalPoints.toLocaleString()}
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                                            <Link
                                                href={`/members/${member.id}`}
                                                className="text-blue-600 hover:text-blue-900 transition-colors"
                                            >
                                                View Details
                                            </Link>
                                        </td>
                                    </tr>
                                ))}
                                {memberPage?.content.length === 0 && (
                                    <tr>
                                        <td colSpan={5} className="px-6 py-12 text-center text-gray-500">
                                            No members found.
                                        </td>
                                    </tr>
                                )}
                            </tbody>
                        </table>
                    </div>

                    {/* Pagination Controls */}
                    {memberPage && memberPage.totalPages > 1 && (
                        <div className="flex justify-between items-center bg-white px-4 py-3 sm:px-6 rounded-lg shadow-sm border border-gray-200">
                            <div className="flex-1 flex justify-between sm:hidden">
                                <button
                                    onClick={() => setCurrentPage(Math.max(0, currentPage - 1))}
                                    disabled={currentPage === 0}
                                    className="relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50"
                                >
                                    Previous
                                </button>
                                <button
                                    onClick={() => setCurrentPage(Math.min(memberPage.totalPages - 1, currentPage + 1))}
                                    disabled={currentPage >= memberPage.totalPages - 1}
                                    className="ml-3 relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50"
                                >
                                    Next
                                </button>
                            </div>
                            <div className="hidden sm:flex-1 sm:flex sm:items-center sm:justify-between">
                                <div>
                                    <p className="text-sm text-gray-700">
                                        Showing <span className="font-medium">{currentPage * memberPage.size + 1}</span> to <span className="font-medium">{Math.min((currentPage + 1) * memberPage.size, memberPage.totalElements)}</span> of{' '}
                                        <span className="font-medium">{memberPage.totalElements}</span> results
                                    </p>
                                </div>
                                <div>
                                    <nav className="relative z-0 inline-flex rounded-md shadow-sm -space-x-px" aria-label="Pagination">
                                        <button
                                            onClick={() => setCurrentPage(Math.max(0, currentPage - 1))}
                                            disabled={currentPage === 0}
                                            className="relative inline-flex items-center px-2 py-2 rounded-l-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50"
                                        >
                                            <span className="sr-only">Previous</span>
                                            &larr;
                                        </button>

                                        {/* Simple page numbers: just show current/total for now to avoid logic complexity */}
                                        <span className="relative inline-flex items-center px-4 py-2 border border-gray-300 bg-white text-sm font-medium text-gray-700">
                                            Page {memberPage.number + 1} of {memberPage.totalPages}
                                        </span>

                                        <button
                                            onClick={() => setCurrentPage(Math.min(memberPage.totalPages - 1, currentPage + 1))}
                                            disabled={currentPage >= memberPage.totalPages - 1}
                                            className="relative inline-flex items-center px-2 py-2 rounded-r-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50"
                                        >
                                            <span className="sr-only">Next</span>
                                            &rarr;
                                        </button>
                                    </nav>
                                </div>
                            </div>
                        </div>
                    )}
                </>
            )}
        </div>
    )
}
