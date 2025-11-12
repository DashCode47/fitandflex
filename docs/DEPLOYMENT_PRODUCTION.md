# 🚀 Fit & Flex API - Deployment a Producción

## 📋 **PROCESO COMPLETO PARA EXPONER API CON DOMINIO**

### **PASO 1: Obtener Dominio y Servidor**

#### **Opción A: Servidor VPS (Recomendado)**
- **DigitalOcean**: $5-10/mes
- **AWS EC2**: $3-8/mes  
- **Google Cloud**: $5-10/mes
- **Linode**: $5/mes

#### **Opción B: Servicios Managed**
- **Heroku**: Gratis (con limitaciones)
- **Railway**: $5/mes
- **Render**: $7/mes
- **Fly.io**: $5/mes

### **PASO 2: Configurar Dominio**

#### **2.1 Comprar Dominio**
- **Namecheap**: $10-15/año
- **GoDaddy**: $12-20/año
- **Google Domains**: $12/año

#### **2.2 Configurar DNS**
```
# Registros DNS necesarios:
A     api.fitandflex.com    → IP_DEL_SERVIDOR
CNAME www.api.fitandflex.com → api.fitandflex.com
```

### **PASO 3: Configurar Servidor**

#### **3.1 Instalar Docker en el servidor**
```bash
# Ubuntu/Debian
curl -fsSL https://get.docker.com -o get-docker.sh
sh get-docker.sh

# Instalar Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
```

#### **3.2 Subir código al servidor**
```bash
# Opción A: Git
git clone https://github.com/tu-usuario/fitandflex.git
cd fitandflex

# Opción B: SCP/SFTP
scp -r . usuario@servidor:/home/usuario/fitandflex/
```

### **PASO 4: Configurar Variables de Entorno**

#### **4.1 Crear archivo .env en el servidor**
```bash
# ===========================================
# FIT & FLEX - PRODUCTION ENVIRONMENT
# ===========================================

# OBLIGATORIAS
JWT_SECRET=TU_CLAVE_SUPER_SECRETA_PARA_PRODUCCION_256_BITS
APP_JWT_SECRET=TU_CLAVE_APP_SECRETA_PARA_PRODUCCION_256_BITS

# BASE DE DATOS
DATABASE_PASSWORD=clave_super_segura_para_produccion
DB_POOL_SIZE=50
DB_MIN_IDLE=10

# CORS - Tu dominio real
CORS_ALLOWED_ORIGINS=https://api.fitandflex.com,https://www.fitandflex.com,https://fitandflex.com

# SSL - Habilitado para producción
SSL_ENABLED=true
SSL_KEYSTORE=/etc/ssl/certs/fitandflex.p12
SSL_KEYSTORE_PASSWORD=clave_ssl_segura
SSL_KEYSTORE_TYPE=PKCS12
SSL_KEY_ALIAS=fitandflex

# LOGGING
LOG_LEVEL=WARN
SECURITY_LOG_LEVEL=ERROR
HIBERNATE_LOG_LEVEL=WARN
SHOW_SQL=false

# RATE LIMITING
RATE_LIMIT_REQUESTS=1000
RATE_LIMIT_WINDOW=60
```

### **PASO 5: Configurar SSL/HTTPS**

#### **5.1 Obtener Certificado SSL**
```bash
# Opción A: Let's Encrypt (Gratis)
sudo apt install certbot
sudo certbot certonly --standalone -d api.fitandflex.com

# Opción B: Certificado comercial
# Comprar en Namecheap, GoDaddy, etc.
```

#### **5.2 Configurar Nginx con SSL**
```nginx
server {
    listen 80;
    server_name api.fitandflex.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name api.fitandflex.com;

    ssl_certificate /etc/letsencrypt/live/api.fitandflex.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/api.fitandflex.com/privkey.pem;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### **PASO 6: Desplegar la Aplicación**

#### **6.1 Ejecutar Docker Compose**
```bash
# En el servidor
cd /home/usuario/fitandflex
docker-compose up -d --build
```

#### **6.2 Verificar que funciona**
```bash
# Verificar contenedores
docker-compose ps

# Ver logs
docker-compose logs -f app

# Probar API
curl https://api.fitandflex.com/actuator/health
```

### **PASO 7: Configurar Firewall**

#### **7.1 Abrir puertos necesarios**
```bash
# Ubuntu/Debian
sudo ufw allow 22    # SSH
sudo ufw allow 80   # HTTP
sudo ufw allow 443  # HTTPS
sudo ufw enable
```

### **PASO 8: Configurar Backup**

#### **8.1 Backup de base de datos**
```bash
# Script de backup diario
#!/bin/bash
docker exec fitandflex-postgres pg_dump -U fitandflex_prod fitandflex_prod > backup_$(date +%Y%m%d).sql
```

#### **8.2 Backup de archivos**
```bash
# Backup de código y configuraciones
tar -czf backup_$(date +%Y%m%d).tar.gz /home/usuario/fitandflex/
```

## 🔧 **COMANDOS ÚTILES**

### **Monitoreo**
```bash
# Ver estado de contenedores
docker-compose ps

# Ver logs en tiempo real
docker-compose logs -f

# Reiniciar aplicación
docker-compose restart app

# Actualizar aplicación
git pull
docker-compose up -d --build
```

### **Mantenimiento**
```bash
# Limpiar contenedores antiguos
docker system prune -a

# Ver uso de recursos
docker stats

# Backup de base de datos
docker exec fitandflex-postgres pg_dump -U fitandflex_prod fitandflex_prod > backup.sql
```

## 📊 **COSTOS ESTIMADOS**

### **Opción Económica (VPS)**
- **Servidor**: $5-10/mes
- **Dominio**: $12/año
- **SSL**: Gratis (Let's Encrypt)
- **Total**: ~$6-12/mes

### **Opción Managed (Heroku/Railway)**
- **Servicio**: $5-10/mes
- **Dominio**: $12/año
- **SSL**: Incluido
- **Total**: ~$6-12/mes

## 🚨 **CONSIDERACIONES DE SEGURIDAD**

1. **Cambiar contraseñas por defecto**
2. **Configurar firewall**
3. **Usar HTTPS siempre**
4. **Backup regular**
5. **Monitoreo de logs**
6. **Actualizaciones de seguridad**

## 📱 **URLs FINALES**

- **API**: `https://api.fitandflex.com`
- **Health Check**: `https://api.fitandflex.com/actuator/health`
- **Login**: `https://api.fitandflex.com/api/auth/login`
- **Documentación**: `https://api.fitandflex.com/swagger-ui.html`

---

**¡Tu API estará disponible públicamente! 🌐**

