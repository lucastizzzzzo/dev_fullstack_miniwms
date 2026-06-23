import React, { useState } from 'react'

export default function ProductForm({ onCreated }){
  const [sku, setSku] = useState('')
  const [nome, setNome] = useState('')
  const [unidade, setUnidade] = useState('')
  const [error, setError] = useState(null)

  function submit(e){
    e.preventDefault()
    setError(null)
    fetch('/api/produtos', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ sku, nome, unidade_medida: unidade })
    }).then(async res=>{
      if(res.ok){
        const data = await res.json()
        onCreated(data)
      } else {
        setError('Erro ao criar produto')
      }
    }).catch(()=>setError('Erro de conexão'))
  }

  return (
    <form onSubmit={submit} className="form">
      {error && <div className="error">{error}</div>}
      <div><label>SKU</label><input value={sku} onChange={e=>setSku(e.target.value)} required /></div>
      <div><label>Nome</label><input value={nome} onChange={e=>setNome(e.target.value)} required /></div>
      <div><label>Unidade</label><input value={unidade} onChange={e=>setUnidade(e.target.value)} /></div>
      <div><button type="submit">Criar</button></div>
    </form>
  )
}
