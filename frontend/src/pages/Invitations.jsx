import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { workspaceAPI } from '../api/workspace';

export default function Invitations() {
  const queryClient = useQueryClient();

  const { data: invitations, isLoading } = useQuery({
    queryKey: ['invitations'],
    queryFn: workspaceAPI.getMyInvitations,
  });

  const acceptMutation = useMutation({
    mutationFn: workspaceAPI.acceptInvitation,
    onSuccess: () => {
      queryClient.invalidateQueries(['invitations']);
      queryClient.invalidateQueries(['workspaces']);
      alert('초대를 수락했습니다');
    },
    onError: (error) => {
      alert(error.response?.data?.message || '초대 수락에 실패했습니다');
    },
  });

  const rejectMutation = useMutation({
    mutationFn: workspaceAPI.rejectInvitation,
    onSuccess: () => {
      queryClient.invalidateQueries(['invitations']);
      alert('초대를 거절했습니다');
    },
    onError: (error) => {
      alert(error.response?.data?.message || '초대 거절에 실패했습니다');
    },
  });

  const handleAccept = (inviteId) => {
    acceptMutation.mutate(inviteId);
  };

  const handleReject = (inviteId) => {
    rejectMutation.mutate(inviteId);
  };

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-xl text-gray-600">로딩 중...</div>
      </div>
    );
  }

  const pendingInvitations = invitations?.filter(inv => inv.status === 'PENDING') || [];

  return (
    <div className="min-h-screen bg-white">
      <div className="max-w-6xl mx-auto px-8 py-8">
        <div className="mb-8">
          <h2 className="text-3xl font-bold text-gray-800">초대 관리</h2>
          <p className="text-gray-600 mt-2">워크스페이스 초대를 확인하고 관리하세요</p>
        </div>

        {pendingInvitations.length > 0 ? (
          <div className="space-y-4">
            {pendingInvitations.map((invitation) => (
              <div
                key={invitation.inviteId}
                className="bg-white p-6 rounded-lg border border-gray-200 hover:border-blue-300 hover:shadow-md transition"
              >
                <div className="flex justify-between items-start">
                  <div className="flex-1">
                    <h3 className="text-xl font-bold text-gray-800 mb-2">
                      {invitation.workspaceName}
                    </h3>
                    <p className="text-gray-600 mb-3">
                      {invitation.workspaceDescription || '설명 없음'}
                    </p>
                    <div className="flex items-center gap-4 text-sm text-gray-500">
                      <span>초대자: {invitation.inviterDisplayName}</span>
                      <span>•</span>
                      <span>역할: {invitation.role}</span>
                    </div>
                  </div>

                  <div className="flex gap-2 ml-4">
                    <button
                      onClick={() => handleAccept(invitation.inviteId)}
                      disabled={acceptMutation.isPending}
                      className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition font-medium disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      {acceptMutation.isPending ? '처리 중...' : '수락'}
                    </button>
                    <button
                      onClick={() => handleReject(invitation.inviteId)}
                      disabled={rejectMutation.isPending}
                      className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition font-medium disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      {rejectMutation.isPending ? '처리 중...' : '거절'}
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="text-center py-16 border border-gray-200 rounded-lg bg-gray-50">
            <svg
              className="mx-auto h-12 w-12 text-gray-400 mb-4"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4"
              />
            </svg>
            <p className="text-gray-500 text-lg">받은 초대가 없습니다</p>
          </div>
        )}
      </div>
    </div>
  );
}
