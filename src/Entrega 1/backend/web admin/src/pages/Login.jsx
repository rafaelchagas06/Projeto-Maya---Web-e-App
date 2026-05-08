import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Stethoscope } from 'lucide-react';
import { supabase } from '../supabaseClient';
import bcrypt from 'bcryptjs';

export default function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      // 1. BUSCA EXCLUSIVA NA TABELA DE PROFISSIONAIS
      // Conforme o requisito de controle de acesso por perfil (Admin/Profissional)
      const { data: profissional, error: dbError } = await supabase
        .from('profissionais')
        .select('*')
        .eq('email', email.trim())
        .single();

      // Se não encontrar na tabela profissionais, o acesso é negado (impede pacientes no web)
      if (dbError || !profissional) {
        setError('Acesso negado. Este portal é exclusivo para profissionais da clínica.');
        setLoading(false);
        return;
      }

      // 2. VALIDAÇÃO DE SEGURANÇA (Hash $2a$12$)
      // Requisito de segurança: senhas com hash e proteção contra acessos indevidos
      if (!profissional.senha || !profissional.senha.startsWith('$2a$12$')) {
        setError('Erro de configuração de segurança. Contacte o administrador.');
        setLoading(false);
        return;
      }

      // 3. COMPARAÇÃO DE SENHA
      const senhaCorreta = bcrypt.compareSync(password, profissional.senha);

      if (senhaCorreta) {
        // Salva a sessão como 'admin' para diferenciar do app mobile
        localStorage.setItem('maya_usuario_logado', JSON.stringify({ 
          id: profissional.id, 
          nome: profissional.nome, 
          tipo: 'admin' 
        }));
        
        navigate('/dashboard');
      } else {
        setError('Senha incorreta. Verifique as suas credenciais.');
      }
    } catch (err) {
      setError('Falha na comunicação com o banco de dados.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', backgroundImage: 'linear-gradient(135deg, #F5F7FA 0%, #E2E8F0 100%)' }}>
      <div className="glass-card" style={{ padding: '48px 40px', width: '100%', maxWidth: '420px' }}>
        <h2 style={{ textAlign: 'center', color: '#0891B2', marginBottom: '8px', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px', fontSize: '28px' }}>
          <Stethoscope size={32} /> Clínica Maya
        </h2>
        <p style={{ textAlign: 'center', color: '#64748B', marginBottom: '32px', fontWeight: '500' }}>Portal do Profissional</p>
        
        {error && (
          <div style={{ color: '#EF4444', background: 'rgba(239, 68, 68, 0.1)', padding: '12px', borderRadius: '8px', marginBottom: '24px', fontSize: '14px', textAlign: 'center', border: '1px solid rgba(239, 68, 68, 0.3)' }}>
            {error}
          </div>
        )}

        <form onSubmit={handleLogin} style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          <div>
            <label className="label-text" style={{ display: 'block', marginBottom: '8px', fontSize: '14px', fontWeight: '600', color: '#475569' }}>E-mail Profissional</label>
            <input 
              type="email" 
              className="input-field" 
              value={email} 
              onChange={(e) => setEmail(e.target.value)} 
              placeholder="seu@email.com" 
              required 
              style={{ width: '100%', padding: '12px', borderRadius: '8px', border: '1px solid #CBD5E1' }}
            />
          </div>
          <div>
            <label className="label-text" style={{ display: 'block', marginBottom: '8px', fontSize: '14px', fontWeight: '600', color: '#475569' }}>Senha</label>
            <input 
              type="password" 
              className="input-field" 
              value={password} 
              onChange={(e) => setPassword(e.target.value)} 
              placeholder="••••••••"
              required 
              style={{ width: '100%', padding: '12px', borderRadius: '8px', border: '1px solid #CBD5E1' }}
            />
          </div>
          <button 
            type="submit" 
            disabled={loading}
            style={{ 
              marginTop: '8px', 
              padding: '14px', 
              fontSize: '16px', 
              background: '#06B6D4',
              color: '#FFFFFF',
              border: 'none',
              borderRadius: '8px',
              fontWeight: '600',
              opacity: loading ? 0.7 : 1, 
              cursor: loading ? 'not-allowed' : 'pointer',
              transition: 'all 0.2s'
            }}
          >
            {loading ? 'AUTENTICANDO...' : 'ACESSAR SISTEMA'}
          </button>
        </form>
      </div>
    </div>
  );
}