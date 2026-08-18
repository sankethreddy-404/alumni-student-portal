import React from 'react';

export default function Badge({ status }) {
  if (!status) return null;
  const cls = `badge badge-${String(status).toLowerCase()}`;
  return <span className={cls}>{status}</span>;
}
