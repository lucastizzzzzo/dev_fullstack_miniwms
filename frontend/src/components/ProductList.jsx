import React, { useEffect, useState } from 'react'
import ProductForm from './ProductForm'

export default function ProductList(){
  const [products, setProducts] = useState([])
  const [showForm, setShowForm] = useState(false)

  useEffect(()=>{
    fetch('/api/produtos')
      .then(res => res.json())
      .then(setProducts)
      .catch(()=>setProducts([]))
  }, [])

  function onCreated(p){
    setProducts(prev => [p, ...prev])
    setShowForm(false)
  }

  return (
    <div>
      <div className="toolbar">
        <button onClick={()=>setShowForm(s=>!s)}>{showForm ? 'Fechar' : 'Novo Produto'}</button>
      </div>
      {showForm && <ProductForm onCreated={onCreated} />}
      <table className="table">
        <thead>
          <tr><th>ID</th><th>SKU</th><th>Nome</th><th>Unidade</th></tr>
        </thead>
        <tbody>
          {products.map(p => (
            <tr key={p.id}><td>{p.id}</td><td>{p.sku}</td><td>{p.nome}</td><td>{p.unidade_medida}</td></tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
