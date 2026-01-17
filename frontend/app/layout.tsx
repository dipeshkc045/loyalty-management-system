import type { Metadata } from 'next'
import { Inter } from 'next/font/google'
import './globals.css'
import Link from 'next/link'
import { MemberProvider } from '@/lib/context/MemberContext'

const inter = Inter({ subsets: ['latin'] })

export const metadata: Metadata = {
    title: 'LMS - Loyalty Management System',
    description: 'Manage customer loyalty programs, points, and rewards',
}

export default function RootLayout({
    children,
}: {
    children: React.ReactNode
}) {
    return (
        <html lang="en">
            <body className={inter.className}>
                <MemberProvider>
                    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-purple-50">
                        {/* Navigation */}
                        <nav className="bg-white shadow-lg border-b border-gray-200">
                            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                                <div className="flex justify-between h-16">
                                    <div className="flex">
                                        <Link href="/" className="flex items-center">
                                            <span className="text-2xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent">
                                                LMS
                                            </span>
                                        </Link>
                                        <div className="hidden sm:ml-8 sm:flex sm:space-x-8">
                                            <Link
                                                href="/"
                                                className="inline-flex items-center px-1 pt-1 text-sm font-medium text-gray-900 hover:text-blue-600 transition-colors"
                                            >
                                                Dashboard
                                            </Link>
                                            <Link
                                                href="/members"
                                                className="inline-flex items-center px-1 pt-1 text-sm font-medium text-gray-700 hover:text-blue-600 transition-colors"
                                            >
                                                Members
                                            </Link>
                                            <Link
                                                href="/transactions"
                                                className="inline-flex items-center px-1 pt-1 text-sm font-medium text-gray-700 hover:text-blue-600 transition-colors"
                                            >
                                                Transactions
                                            </Link>
                                            <Link
                                                href="/rules"
                                                className="inline-flex items-center px-1 pt-1 text-sm font-medium text-gray-700 hover:text-blue-600 transition-colors"
                                            >
                                                Rules
                                            </Link>
                                            <Link
                                                href="/events"
                                                className="inline-flex items-center px-1 pt-1 text-sm font-medium text-gray-700 hover:text-blue-600 transition-colors"
                                            >
                                                Events
                                            </Link>
                                            <Link
                                                href="/products"
                                                className="inline-flex items-center px-1 pt-1 text-sm font-medium text-gray-700 hover:text-blue-600 transition-colors"
                                            >
                                                Products
                                            </Link>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </nav>

                        {/* Main Content */}
                        <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                            {children}
                        </main>
                    </div>
                </MemberProvider>
            </body>
        </html>
    )
}
